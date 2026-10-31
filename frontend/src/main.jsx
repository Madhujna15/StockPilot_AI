import React from 'react'
import { createRoot } from 'react-dom/client'
import Lenis from 'lenis'
import 'lenis/dist/lenis.css'
import './style.css'
import App from './App.jsx'

const lenis = new Lenis({ lerp: 0.08, smoothWheel: true })
function raf(time) {
  lenis.raf(time)
  requestAnimationFrame(raf)
}
requestAnimationFrame(raf)

createRoot(document.getElementById('root')).render(
  <React.StrictMode><App /></React.StrictMode>,
)
