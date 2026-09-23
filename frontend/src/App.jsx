import { useState } from 'react'
import Editor from '@monaco-editor/react'
import axios from 'axios'

function App() {
  const [sqlCode, setSqlCode] = useState('-- Write your SQL migration script here\n\nCREATE TABLE test_table (\n    id SERIAL PRIMARY KEY\n);')
  const [status, setStatus] = useState('Ready')

  // Week 2: Metrics State
  const [replayCount, setReplayCount] = useState(0)
  const [errorRate, setErrorRate] = useState(0)

  const runScript = async () => {
    setStatus('Spinning up temporary sandbox container...')
    try {
      // 1. Start the Docker container
      const startRes = await axios.post('http://localhost:8080/api/v1/containers/start')
      const containerId = startRes.data.containerId
      setStatus(`Container active. Executing SQL...`)

      // 2. Inject the schema from the editor
      const execRes = await axios.post(`http://localhost:8080/api/v1/containers/${containerId}/execute`, sqlCode, {
        headers: { 'Content-Type': 'text/plain' }
      })
      setStatus(`  ${execRes.data}`)

      // Update metrics on success
      setReplayCount(prev => prev + 1)

    } catch (error) {
      setStatus(`  Error: ${error.response?.data || error.message}`)
      // Bump error rate on failure (mocked logic for UI scaffolding)
      setErrorRate(prev => Math.min(100, prev + 10))
    }
  }

  return (
    <div style={{ height: '100vh', display: 'flex', flexDirection: 'column', fontFamily: 'sans-serif' }}>
      {/* Top Navbar */}
      <header style={{ padding: '15px 20px', backgroundColor: '#1e1e1e', color: '#fff', borderBottom: '1px solid #333', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2 style={{ margin: 0 }}>ShadowBase Sandbox</h2>
        <button onClick={runScript} style={{ padding: '10px 20px', backgroundColor: '#007acc', color: 'white', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}>
          Run Migration Script
        </button>
      </header>

      {/* Week 2: Metrics Dashboard */}
      <div style={{ padding: '15px 20px', backgroundColor: '#252526', display: 'flex', gap: '40px', borderBottom: '1px solid #333' }}>
        <div style={{ display: 'flex', flexDirection: 'column' }}>
          <span style={{ fontSize: '12px', color: '#858585', textTransform: 'uppercase', letterSpacing: '1px' }}>Query Replays</span>
          <span style={{ fontSize: '28px', fontWeight: 'bold', color: '#4caf50' }}>{replayCount}</span>
        </div>
        <div style={{ display: 'flex', flexDirection: 'column' }}>
          <span style={{ fontSize: '12px', color: '#858585', textTransform: 'uppercase', letterSpacing: '1px' }}>Error Rate</span>
          <span style={{ fontSize: '28px', fontWeight: 'bold', color: '#f44336' }}>{errorRate}%</span>
        </div>
      </div>

      {/* Status Bar */}
      <div style={{ padding: '10px 20px', backgroundColor: '#2d2d2d', color: '#4caf50', fontSize: '14px', borderBottom: '1px solid #333' }}>
        Status: {status}
      </div>

      {/* Monaco Editor */}
      <div style={{ flexGrow: 1 }}>
        <Editor
          height="100%"
          defaultLanguage="sql"
          theme="vs-dark"
          value={sqlCode}
          onChange={(value) => setSqlCode(value)}
          options={{
            minimap: { enabled: false },
            fontSize: 16,
            wordWrap: "on"
          }}
        />
      </div>
    </div>
  )
}

export default App