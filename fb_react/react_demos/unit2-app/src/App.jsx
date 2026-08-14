import { useState } from 'react'
import DemoJsxBasics from './demos/DemoJsxBasics'
import DemoConditionalList from './demos/DemoConditionalList'
import DemoComponentProps from './demos/DemoComponentProps'
import DemoUseStateCounter from './demos/DemoUseStateCounter'
import DemoUseStateTodos from './demos/DemoUseStateTodos'
import DemoControlledForm from './demos/DemoControlledForm'
import DemoUseEffectFetch from './demos/DemoUseEffectFetch'
import DemoUseEffectCleanup from './demos/DemoUseEffectCleanup'
import DemoUserSearch from './demos/DemoUserSearch'

// 資料陣列驅動 UI：每個 demo 只要在此加一行
const demos = [
  { key: 'jsx',        label: '01 JSX 語法',      Component: DemoJsxBasics },
  { key: 'conditional', label: '02 條件/列表',     Component: DemoConditionalList },
  { key: 'props',      label: '03 元件/Props',     Component: DemoComponentProps },
  { key: 'counter',    label: '04 useState',       Component: DemoUseStateCounter },
  { key: 'todos',      label: '05 陣列/物件',      Component: DemoUseStateTodos },
  { key: 'form',       label: '06 受控表單',       Component: DemoControlledForm },
  { key: 'fetch',      label: '07 useEffect API',  Component: DemoUseEffectFetch },
  { key: 'cleanup',    label: '08 Cleanup',        Component: DemoUseEffectCleanup },
  { key: 'search',     label: '09 綜合實作',       Component: DemoUserSearch },
]

function App() {
  const [currentKey, setCurrentKey] = useState('jsx')
  const current = demos.find(d => d.key === currentKey)
  const CurrentDemo = current.Component

  return (
    <>
      <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
        <div className="container-fluid">
          <span className="navbar-brand">Unit 2 · React 核心概念</span>
          <div className="collapse navbar-collapse">
            <ul className="navbar-nav me-auto">
              {demos.map(({ key, label }) => (
                <li className="nav-item" key={key}>
                  <a
                    className={`nav-link ${currentKey === key ? 'active' : ''}`}
                    href="#"
                    onClick={e => {
                      e.preventDefault()
                      setCurrentKey(key)
                    }}
                  >
                    {label}
                  </a>
                </li>
              ))}
            </ul>
          </div>
        </div>
      </nav>
      <CurrentDemo />
    </>
  )
}

export default App
