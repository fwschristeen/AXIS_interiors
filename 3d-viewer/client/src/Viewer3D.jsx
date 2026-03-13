import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import { Canvas } from '@react-three/fiber';
import { OrbitControls, Environment, ContactShadows } from '@react-three/drei';
import Scene from './Scene';

const Viewer3D = () => {
    const { id } = useParams();
    const [design, setDesign] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        axios.get(`http://localhost:3001/api/designs/${id}`)
            .then(res => setDesign(res.data))
            .catch(err => console.error(err))
            .finally(() => setLoading(false));
    }, [id]);

    if (loading) return <div style={{display:'flex',justifyContent:'center',alignItems:'center',height:'100vh',backgroundColor:'#1a1a1a',color:'#fff'}}>Loading 3D View...</div>;
    if (!design) return <div style={{display:'flex',justifyContent:'center',alignItems:'center',height:'100vh',backgroundColor:'#1a1a1a',color:'#fff'}}>Design not found! Please save a design in the Desktop App first.</div>;

    return (
        <div style={{ width: '100vw', height: '100vh', backgroundColor: '#1a1a1a' }}>
            <Canvas shadows camera={{ position: [6, 6, 6], fov: 45 }}>
                <ambientLight intensity={0.4} />
                <directionalLight castShadow position={[5, 10, 5]} intensity={1.5} shadow-mapSize={[1024, 1024]} />
                <Environment preset="city" />
                <Scene design={design} />
                <OrbitControls makeDefault minPolarAngle={0} maxPolarAngle={Math.PI / 2 - 0.05} />
                <ContactShadows position={[0, -0.01, 0]} opacity={0.6} scale={20} blur={1.5} far={10} />
            </Canvas>
            
            {/* Overlay Info */}
            <div style={{ position: 'absolute', top: 20, left: 20, color: 'white', background: 'rgba(20,20,30,0.8)', padding: '20px', borderRadius: '12px', zIndex: 10, fontFamily: 'Segoe UI, sans-serif', boxShadow: '0 4px 15px rgba(0,0,0,0.3)', backdropFilter: 'blur(5px)', border: '1px solid rgba(255,255,255,0.1)' }}>
                <h2 style={{margin: '0 0 10px 0', fontSize: '20px', fontWeight: 'bold'}}>{design.room?.name || 'My Design'}</h2>
                <div style={{fontSize: '14px', color: '#a0a0c8'}}>
                    <p style={{margin: '0 0 5px 0'}}>Dimensions: {design.room?.width}m &times; {design.room?.depth}m</p>
                    <p style={{margin: '0 0 5px 0'}}>Items: <b style={{color: '#fff'}}>{design.furnitureItems?.length || 0}</b></p>
                </div>
                <div style={{marginTop: '15px', paddingTop: '10px', borderTop: '1px solid rgba(255,255,255,0.1)', fontSize: '12px', color: '#00D4AA'}}>
                    Drag to rotate &bull; Scroll to zoom
                </div>
            </div>
        </div>
    );
};

export default Viewer3D;
