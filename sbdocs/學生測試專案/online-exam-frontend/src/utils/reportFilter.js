function round1(v) { return Math.round(v * 10) / 10 }

/** 由完整報表取出可選的科目清單（依 examId 去重，保留原始順序） */
export function reportExamOptions(report) {
  if (!report?.items) return []
  const seen = new Set()
  const options = []
  for (const it of report.items) {
    if (seen.has(it.examId)) continue
    seen.add(it.examId)
    options.push({ examId: it.examId, examTitle: it.examTitle })
  }
  return options
}

/** 依選定科目（examId 為空＝全部）過濾學生成績報表並重新計算統計 */
export function filterStudentReport(report, examId) {
  if (!report || !examId) return report
  const items = report.items.filter(i => String(i.examId) === String(examId))
  const visible = items.filter(i => !i.scoreHidden)
  const pcts = visible.map(i => i.percentage)

  const gradeDistribution = { A: 0, B: 0, C: 0, D: 0, F: 0 }
  visible.forEach(i => {
    if (gradeDistribution[i.grade] != null) gradeDistribution[i.grade] += 1
  })

  return {
    ...report,
    items,
    examCount: items.length,
    totalAttempts: items.reduce((s, i) => s + i.attemptCount, 0),
    averagePercentage: pcts.length ? round1(pcts.reduce((a, b) => a + b, 0) / pcts.length) : 0,
    bestPercentage: pcts.length ? round1(Math.max(...pcts)) : 0,
    lowestPercentage: pcts.length ? round1(Math.min(...pcts)) : 0,
    gradeDistribution,
  }
}
