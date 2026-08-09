import { useState } from 'react'
import Demo01Mount from './demos/Demo01Mount'
import Demo02UseState from './demos/Demo02UseState'
import Demo03EventHandlers from './demos/Demo03EventHandlers'
import Demo04ConditionalRender from './demos/Demo04ConditionalRender'
import Demo05FetchAsync from './demos/Demo05FetchAsync'
import Demo06NavbarMap from './demos/Demo06NavbarMap'
import Demo07ControlledInput from './demos/Demo07ControlledInput'
import Demo08UseEffect from './demos/Demo08UseEffect'
import Demo09ReduceCart from './demos/Demo09ReduceCart'
import Demo10PromiseAll from './demos/Demo10PromiseAll'

// 資料陣列驅動 UI：每一個 demo 只要在此加一行
const demos = [
  { key: 'demo01', label: '01 掛載點',      Component: Demo01Mount },
  { key: 'demo02', label: '02 useState',    Component: Demo02UseState },
  { key: 'demo03', label: '03 事件處理',    Component: Demo03EventHandlers },
  { key: 'demo04', label: '04 條件渲染',    Component: Demo04ConditionalRender },
  { key: 'demo05', label: '05 fetch/async', Component: Demo05FetchAsync },
  { key: 'demo06', label: '06 .map()',      Component: Demo06NavbarMap },
  { key: 'demo07', label: '07 受控輸入',    Component: Demo07ControlledInput },
  { key: 'demo08', label: '08 useEffect',   Component: Demo08UseEffect },
  { key: 'demo09', label: '09 reduce',      Component: Demo09ReduceCart },
  { key: 'demo10', label: '10 Promise.all', Component: Demo10PromiseAll },
]

function App() {
  const [currentKey, setCurrentKey] = useState('demo01')
  const current = demos.find(d => d.key === currentKey)
  const CurrentDemo = current.Component

  return (
    <>
      <nav className="navbar navbar-expand navbar-dark bg-dark">
        <div className="container-fluid">
          <span className="navbar-brand">React Vite Demos</span>
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
