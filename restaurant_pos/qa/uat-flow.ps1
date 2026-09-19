$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

$proxy = 'http://localhost:5173/api'
$mysql = 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'
$env:MYSQL_PWD = '1234'
$bodyDir = Join-Path $env:TEMP 'pos_uat'
New-Item -ItemType Directory -Force -Path $bodyDir | Out-Null

$results = @()
$createdOrders = @()
$createdEmp = $null
$createdClosing = $null

function Get-Api($url) { return ((Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 10).Content | ConvertFrom-Json) }
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
    $today = Get-Date -Format 'yyyy-MM-dd'

    # U-01 員工登入（SC-01）
    Post-Api "$proxy/employees" '{"username":"uat_staff","password":"uat123","name":"UAT 人員","role":"STAFF"}' | Out-Null
    $createdEmp = 'uat_staff'
    $login = Post-Api "$proxy/auth/login" '{"username":"uat_staff","password":"uat123"}'
    Add-Result 'U-01' '現職員工登入成功' ($login.code -eq 0 -and $login.data.username -eq 'uat_staff') "code=$($login.code) name=$($login.data.name) role=$($login.data.role)"
    $empId = $login.data.id
    $badLogin = Post-Api "$proxy/auth/login" '{"username":"uat_staff","password":"wrong"}'
    Add-Result 'U-01b' '錯誤密碼登入被拒' ($badLogin.code -eq 401) "code=$($badLogin.code)"

    # U-02 桌位開單（SC-06）
    $tbl = @((Get-Api "$proxy/tables").data | Where-Object status -eq 'AVAILABLE' | Select-Object -First 1)
    $r = Post-Api "$proxy/orders" ('{"tableId":' + $tbl.id + ',"employeeId":' + $empId + '}')
    $orderId = $r.data.id; $createdOrders += $orderId
    Add-Result 'U-02' '可用桌位開單成功' ($r.code -eq 0) "order=$orderId 桌位=$($tbl.tableNumber)"

    # U-03 點餐含飲料選項（SC-07）
    $r = Post-Api "$proxy/orders/$orderId/items" '{"items":[{"menuItemId":4,"quantity":1,"sugarLevel":"半糖","iceLevel":"去冰","note":null},{"menuItemId":2,"quantity":2}]}'
    $n = @($r.data.items).Count
    $drink = @($r.data.items | Where-Object menuItemId -eq 4 | Select-Object -First 1)
    Add-Result 'U-03' '點餐完成(餐+飲規格鍵入)' ($r.code -eq 0 -and $n -eq 2 -and $drink.sugarLevel -eq '半糖' -and $drink.iceLevel -eq '去冰') "items=$n sugar=$($drink.sugarLevel) ice=$($drink.iceLevel)"
    Add-Result 'U-03b' '總額=40+65*2=170' ($r.data.totalAmount -eq 170) "total=$($r.data.totalAmount)"

    # U-04 結帳收款（SC-08）
    $r = Post-Api "$proxy/orders/$orderId/payment" ('{"paymentMethod":"CREDIT_CARD","amount":170,"employeeId":' + $empId + '}')
    Add-Result 'U-04' '信用卡收款成功' ($r.code -eq 0 -and $r.data.paymentMethod -eq 'CREDIT_CARD') "code=$($r.code) method=$($r.data.paymentMethod) paidAt=$($r.data.paidAt)"

    # U-05 交易報表含今日交易（SC-10）
    $rep = Get-Api "$proxy/reports/transactions?from=$today&to=$today"
    $mine = @($rep.data | Where-Object orderId -eq $orderId | Select-Object -First 1)
    Add-Result 'U-05' '報表查得本日交易' ($rep.code -eq 0 -and $mine.orderId -eq $orderId) "計數=$(@($rep.data).Count) row=$($mine.orderId) 金額=$($mine.totalAmount) 員工=$($mine.employeeName) 桌號=$($mine.tableNumber)"

    # U-06 每日結帳含本單金額（SC-09）
    $cl = Post-Api "$proxy/closings" ('{"closingDate":"' + $today + '","employeeId":1}')
    if ($cl.code -eq 0) { $createdClosing = $cl.data.closingDate }
    Add-Result 'U-06' '本日結帳成功且含本單' ($cl.code -eq 0 -and $cl.data.totalOrders -eq 1 -and $cl.data.cardAmount -eq 170) "orders=$($cl.data.totalOrders) revenue=$($cl.data.totalRevenue) cash=$($cl.data.cashAmount) card=$($cl.data.cardAmount)"

    # U-02b 桌位狀態由管理功能維護（SC-04，符合需求書 §3(3)「可擴充」）
    $set = Invoke-RestMethod -Method Patch -Uri "$proxy/tables/$($tbl.id)/status?status=OCCUPIED" -TimeoutSec 10
    $occupied = @($set.data)
    Add-Result 'U-02b' 'SC-04 手動設為占用成功' ($set.code -eq 0 -and $occupied.status -eq 'OCCUPIED') "code=$($set.code) status=$($occupied.status)"
}
catch {
    Write-Host "!! UAT 例外: $($_.Exception.Message)"
}
finally {
    $sql = @()
    if ($createdOrders.Count -gt 0) {
        $in = ($createdOrders -join ',')
        $sql += "DELETE FROM restaurant_pos.payment WHERE order_id IN ($in);"
        $sql += "DELETE FROM restaurant_pos.order_item WHERE order_id IN ($in);"
        $sql += "DELETE FROM restaurant_pos.orders WHERE id IN ($in);"
        $sql += "UPDATE restaurant_pos.restaurant_table SET status='AVAILABLE' WHERE id=$($tbl.id);"
    }
    if ($createdEmp) { $sql += "DELETE FROM restaurant_pos.employee WHERE username='$createdEmp';" }
    if ($createdClosing) { $sql += "DELETE FROM restaurant_pos.daily_closing WHERE closing_date='$createdClosing';" }
    if ($sql.Count -gt 0) { & $mysql -uroot -e $($sql -join ' ') 2>&1 | Out-Null; Write-Host "UAT 清理完成" }
    if ($createdOrders.Count -gt 0) {
        $tblId = $null
    }
    Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
}

Write-Host ''
Write-Host '======== UAT 自動化預檢摘要 ========'
$results | Format-Table -AutoSize
$pass = @($results | Where-Object Result -eq 'PASS').Count
$fail = @($results | Where-Object Result -eq 'FAIL').Count
Write-Host "統計: PASS=$pass FAIL=$fail"