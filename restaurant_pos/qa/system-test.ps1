$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$base = 'http://localhost:8080/api'
$mysql = 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'
$bodyDir = Join-Path $env:TEMP 'pos_qa'
New-Item -ItemType Directory -Force -Path $bodyDir | Out-Null

$results = @()
$createdOrders = @()
$createdTableNo = $null
$createdEmp = $null
$createdClosingDate = $null

function Get-Api($url) {
    return ((Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 10).Content | ConvertFrom-Json)
}

function Post-Api($url, $json) {
    $f = Join-Path $bodyDir ('b_' + [guid]::NewGuid().ToString('N') + '.json')
    [IO.File]::WriteAllText($f, $json, (New-Object System.Text.UTF8Encoding($false)))
    $out = curl.exe -s -X POST $url -H "Content-Type: application/json" --data-binary "@$f"
    Remove-Item $f -ErrorAction SilentlyContinue
    return ($out | ConvertFrom-Json)
}

function Add-Result($id, $name, $cond, $detail) {
    $r = if ($cond) { 'PASS' } else { 'FAIL' }
    $script:results += [pscustomobject]@{ ID = $id; Name = $name; Result = $r; Detail = $detail }
}

try {
    # ---- 前置參考資料 ----
    $tables = (Get-Api "$base/tables").data
    $tbl = @($tables | Where-Object status -eq 'AVAILABLE' | Select-Object -First 1)
    $tblId = $tbl.id
    $newNo = ([int[]]$tables.tableNumber | Measure-Object -Maximum).Maximum + 1
    $emps = (Get-Api "$base/employees").data
    $adminId = ($emps | Where-Object role -eq 'ADMIN' | Select-Object -First 1).id
    Write-Host ("前置: 可用桌位 id=$tblId, 新桌號=$newNo, admin id=$adminId")

    # === T-06 FR-01 帳號唯一 / NFR-04 BCrypt ===
    $r = Post-Api "$base/employees" '{"username":"qa_staff","password":"qa1234","name":"QA 人員","role":"STAFF"}'
    if ($r.code -eq 0) { $createdEmp = $r.data.username }
    Add-Result 'T-06a' '建立員工成功' ($r.code -eq 0) "code=$($r.code)"
    $r2 = Post-Api "$base/employees" '{"username":"qa_staff","password":"x","name":"x","role":"STAFF"}'
    Add-Result 'T-06b' '重複帳號拒絕(4091)' ($r2.code -eq 4091) "code=$($r2.code)"
    $env:MYSQL_PWD = '1234'
    $hash = (& $mysql -N -uroot -e "SELECT password FROM restaurant_pos.employee WHERE username='qa_staff'")
    Add-Result 'T-06c' '密碼為 BCrypt 非明碼' ($hash -match '^\$2[aby]\$') "hash=$hash"
    $login = Post-Api "$base/auth/login" '{"username":"qa_staff","password":"qa1234"}'
    Add-Result 'T-06d' '登入成功(密碼驗證)' ($login.code -eq 0 -and $login.data.username -eq 'qa_staff') "code=$($login.code) role=$($login.data.role)"

    # === T-05 FR-02 桌號唯一 ===
    $r = Post-Api "$base/tables" ('{"tableNumber":' + $newNo + ',"capacity":2}')
    if ($r.code -eq 0) { $createdTableNo = $newNo }
    Add-Result 'T-05a' '建立桌位成功' ($r.code -eq 0) "code=$($r.code)"
    $r2 = Post-Api "$base/tables" ('{"tableNumber":' + $newNo + ',"capacity":4}')
    Add-Result 'T-05b' '重複桌號拒絕(4091)' ($r2.code -eq 4091) "code=$($r2.code) msg=$($r2.message)"

    # === T-01 BR-01 小計計算 (unit=40, qty=2 -> 80) ===
    $empId = $login.data.id
    $r = Post-Api "$base/orders" ('{"tableId":' + $tblId + ',"employeeId":' + $empId + '}')
    $orderA = $r.data.id
    $createdOrders += $orderA
    $r = Post-Api "$base/orders/$orderA/items" '{"items":[{"menuItemId":4,"quantity":2,"sugarLevel":"半糖","iceLevel":"微冰","note":null}]}'
    $line = @($r.data.items | Where-Object menuItemId -eq 4 | Select-Object -First 1)
    Add-Result 'T-01' '小計=40*2=80' ($line.subtotal -eq 80) "subtotal=$($line.subtotal)"

    # === T-02 BR-02 訂單總額 (65+50=115) ===
    $r = Post-Api "$base/orders" ('{"tableId":' + $tblId + ',"employeeId":' + $empId + '}')
    $orderB = $r.data.id
    $createdOrders += $orderB
    $r = Post-Api "$base/orders/$orderB/items" '{"items":[{"menuItemId":2,"quantity":1},{"menuItemId":3,"quantity":1}]}'
    Add-Result 'T-02' '總額=65+50=115' ($r.data.totalAmount -eq 115) "total=$($r.data.totalAmount)"

    # === T-07 FR-03 停售項目下單 ===
    $r = Post-Api "$base/orders/$orderA/items" '{"items":[{"menuItemId":1,"quantity":1}]}'
    Add-Result 'T-07' '停售項目拒絕下單' ($r.code -eq 4001) "code=$($r.code) msg=$($r.message)"

    # === T-04 BR-04 付款狀態流 ===
    $r = Post-Api "$base/orders/$orderB/payment" ('{"paymentMethod":"CASH","amount":115,"employeeId":' + $empId + '}')
    Add-Result 'T-04a' '收款成功' ($r.code -eq 0) "code=$($r.code)"
    $g = (Get-Api "$base/orders/$orderB").data
    Add-Result 'T-04b' 'status=PAID 且 paidAt 非空' ($g.status -eq 'PAID' -and $g.paidAt) "status=$($g.status) paidAt=$($g.paidAt)"

    # === T-03 BR-03 一單一付款 ===
    $r = Post-Api "$base/orders/$orderB/payment" ('{"paymentMethod":"CASH","amount":1,"employeeId":' + $empId + '}')
    Add-Result 'T-03' '已付款再收款拒絕(4091)' ($r.code -eq 4091) "code=$($r.code) msg=$($r.message)"

    # === T-08 FR-04 已付款加點 ===
    $r = Post-Api "$base/orders/$orderB/items" '{"items":[{"menuItemId":2,"quantity":1}]}'
    Add-Result 'T-08' 'PAID 訂單加點被拒' ($r.code -eq 4001 -or $r.code -eq 4091) "code=$($r.code) msg=$($r.message)"

    # === T-10 NFR-05 時區 (UTC+8) ===
    $before = (Get-Date).AddMinutes(-3)
    $r = Post-Api "$base/orders/$orderA/payment" ('{"paymentMethod":"LINE_PAY","amount":80,"employeeId":' + $empId + '}')
    $paidAt = [datetime]::Parse($r.data.paidAt)
    $after = (Get-Date).AddMinutes(3)
    $utcDrift = [math]::Round((($paidAt - [datetime]::UtcNow).TotalHours), 1)
    Add-Result 'T-10' 'paidAt 為本機本地時間(UTC+8)' (($paidAt -gt $before -and $paidAt -lt $after) -and [math]::Abs($utcDrift - 8) -lt 1) "paidAt=$($r.data.paidAt) 對UTC偏移=${utcDrift}h"

    # === T-09 FR-06 重複結帳 ===
    $r = Post-Api "$base/closings" ('{"closingDate":"2099-12-31","employeeId":' + $adminId + '}')
    if ($r.code -eq 0) { $createdClosingDate = $r.data.closingDate }
    Add-Result 'T-09a' '首次結帳成功' ($r.code -eq 0) "code=$($r.code) totalOrders=$($r.data.totalOrders) revenue=$($r.data.totalRevenue)"
    $r2 = Post-Api "$base/closings" ('{"closingDate":"2099-12-31","employeeId":' + $adminId + '}')
    Add-Result 'T-09b' '同日重複結帳拒絕(4091)' ($r2.code -eq 4091) "code=$($r2.code) msg=$($r2.message)"
}
catch {
    Write-Host "!! 測試執行例外: $($_.Exception.Message)"
}
finally {
    # ---- 清理測試資料 ----
    $sql = @()
    if ($createdOrders.Count -gt 0) {
        $in = ($createdOrders -join ',')
        $sql += "DELETE FROM restaurant_pos.order_item WHERE order_id IN ($in);"
        $sql += "DELETE FROM restaurant_pos.payment WHERE order_id IN ($in);"
        $sql += "DELETE FROM restaurant_pos.orders WHERE id IN ($in);"
    }
    if ($createdTableNo) { $sql += "DELETE FROM restaurant_pos.restaurant_table WHERE table_number=$createdTableNo;" }
    if ($createdEmp) { $sql += "DELETE FROM restaurant_pos.employee WHERE username='$createdEmp';" }
    if ($createdClosingDate) { $sql += "DELETE FROM restaurant_pos.daily_closing WHERE closing_date='$createdClosingDate';" }
    if ($sql.Count -gt 0) {
        & $mysql -uroot -e $($sql -join ' ') 2>&1 | Out-Null
        Write-Host "清理完成: $($sql.Count) 組語句"
    }
    Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
}

# ---- 摘要輸出 ----
Write-Host ""
Write-Host '======== 系統測試摘要 (T-01 ~ T-10) ========'
$results | Format-Table -AutoSize
$pass = @($results | Where-Object Result -eq 'PASS').Count
$fail = @($results | Where-Object Result -eq 'FAIL').Count
Write-Host "統計: PASS=$pass FAIL=$fail"
$results | ConvertTo-Json -Depth 4 | Out-File (Join-Path $env:TEMP 'pos_qa_summary.json')