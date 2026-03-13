import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Viewer3D from './Viewer3D';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/view/:id" element={<Viewer3D />} />
        <Route path="*" element={<div style={{color:'white', background:'#111', height:'100vh', display:'flex', justifyContent:'center', alignItems:'center'}}>Please provide a design ID: /view/:id</div>} />
      </Routes>
    </Router>
  );
}

export default App;
