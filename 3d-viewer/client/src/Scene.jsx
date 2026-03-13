import React, { useMemo } from 'react';
import * as THREE from 'three';
import { RoundedBox } from '@react-three/drei';

function getMaterial(color, type = 'standard') {
    if (type === 'wood') {
        return <meshStandardMaterial color={color} roughness={0.8} metalness={0.1} />;
    } else if (type === 'fabric') {
        return <meshStandardMaterial color={color} roughness={0.9} metalness={0.0} />;
    } else if (type === 'metal') {
        return <meshStandardMaterial color={color} roughness={0.3} metalness={0.8} />;
    } else if (type === 'glass') {
        return <meshPhysicalMaterial color={color} transmission={0.9} opacity={1} roughness={0.1} ior={1.5} thickness={0.05} />;
    } else if (type === 'screen') {
        return <meshStandardMaterial color={color} roughness={0.2} metalness={0.8} emissive={new THREE.Color(0.1, 0.1, 0.1)} />;
    } else if (type === 'mirror_glass') {
        return <meshStandardMaterial color="#ffffff" roughness={0.0} metalness={1.0} envMapIntensity={2.0} />;
    }
    return <meshStandardMaterial color={color} roughness={0.6} metalness={0.1} />;
}

// ---- TABLE ----
const Table3D = ({ w, d, h, color }) => {
    const topH = h * 0.08;
    const legH = h - topH;
    const legW = w * 0.06;
    const legD = d * 0.06;
    return (
        <group>
            {/* Table Top */}
            <mesh position={[0, legH + topH/2, 0]} castShadow receiveShadow>
                <boxGeometry args={[w, topH, d]} />
                {getMaterial(color, 'wood')}
            </mesh>
            {/* 4 Legs */}
            {[[-1,-1], [-1,1], [1,-1], [1,1]].map(([x, z], i) => (
                <mesh key={i} position={[(w/2 - legW/2 - w*0.02)*x, legH/2, (d/2 - legD/2 - d*0.02)*z]} castShadow receiveShadow>
                    <boxGeometry args={[legW, legH, legD]} />
                    {getMaterial(color, 'wood')}
                </mesh>
            ))}
        </group>
    );
};

// ---- SOFA ----
const Sofa3D = ({ w, d, h, color }) => {
    const seatH = h * 0.4;
    const armW = w * 0.12;
    const backD = d * 0.2;
    return (
        <group>
            {/* Seat Base */}
            <RoundedBox args={[w, seatH, d - backD]} position={[0, seatH/2, backD/2]} radius={0.05} castShadow receiveShadow>
                {getMaterial(color, 'fabric')}
            </RoundedBox>
            {/* Backrest */}
            <RoundedBox args={[w, h, backD]} position={[0, h/2, -d/2 + backD/2]} radius={0.05} castShadow receiveShadow>
                {getMaterial(color, 'fabric')}
            </RoundedBox>
            {/* Left Armrest */}
            <RoundedBox args={[armW, h*0.7, d - backD]} position={[-w/2 + armW/2, (h*0.7)/2, backD/2]} radius={0.05} castShadow receiveShadow>
                {getMaterial(color, 'fabric')}
            </RoundedBox>
            {/* Right Armrest */}
            <RoundedBox args={[armW, h*0.7, d - backD]} position={[w/2 - armW/2, (h*0.7)/2, backD/2]} radius={0.05} castShadow receiveShadow>
                {getMaterial(color, 'fabric')}
            </RoundedBox>
            {/* Cushions */}
            <RoundedBox args={[w - armW*2 - 0.05, seatH*0.4, d - backD - 0.05]} position={[0, seatH + seatH*0.2, backD/2 + 0.02]} radius={0.05} castShadow receiveShadow>
                {getMaterial('#eee', 'fabric')}
            </RoundedBox>
        </group>
    );
};

// ---- CHAIR ----
const Chair3D = ({ w, d, h, color }) => {
    const seatH = h * 0.45;
    const legW = w * 0.08;
    const legD = d * 0.08;
    return (
        <group>
            {/* Seat */}
            <mesh position={[0, seatH, 0]} castShadow receiveShadow>
                <boxGeometry args={[w, h * 0.08, d]} />
                {getMaterial(color, 'wood')}
            </mesh>
            {/* Legs */}
            {[[-1,-1], [-1,1], [1,-1], [1,1]].map(([x, z], i) => (
                <mesh key={i} position={[(w/2 - legW/2)*x, seatH/2, (d/2 - legD/2)*z]} castShadow receiveShadow>
                    <boxGeometry args={[legW, seatH, legD]} />
                    {getMaterial(color, 'wood')}
                </mesh>
            ))}
            {/* Backrest */}
            <mesh position={[0, seatH + (h - seatH)/2, -d/2 + d*0.05]} castShadow receiveShadow>
                <boxGeometry args={[w, h - seatH, d * 0.1]} />
                {getMaterial(color, 'wood')}
            </mesh>
        </group>
    );
};

// ---- BED ----
const Bed3D = ({ w, d, h, color }) => {
    const frameH = h * 0.3;
    const mattressH = h * 0.25;
    const headboardH = h;
    const headboardD = d * 0.06;
    return (
        <group>
            {/* Headboard */}
            <mesh position={[0, headboardH/2, -d/2 + headboardD/2]} castShadow receiveShadow>
                <boxGeometry args={[w, headboardH, headboardD]} />
                {getMaterial('#8B5A2B', 'wood')}
            </mesh>
            {/* Frame */}
            <mesh position={[0, frameH/2, headboardD/2]} castShadow receiveShadow>
                <boxGeometry args={[w, frameH, d - headboardD]} />
                {getMaterial('#8B5A2B', 'wood')}
            </mesh>
            {/* Mattress */}
            <RoundedBox args={[w*0.96, mattressH, d - headboardD - 0.1]} position={[0, frameH + mattressH/2, headboardD/2]} radius={0.05} castShadow receiveShadow>
                {getMaterial('#F0EBDC', 'fabric')}
            </RoundedBox>
            {/* Blanket */}
            <RoundedBox args={[w*0.98, h*0.04, d*0.6]} position={[0, frameH + mattressH + h*0.02, headboardD/2 + d*0.15]} radius={0.02} castShadow receiveShadow>
                {getMaterial(color, 'fabric')}
            </RoundedBox>
            {/* Pillows */}
            <RoundedBox args={[w*0.35, mattressH*0.4, d*0.15]} position={[-w*0.25, frameH + mattressH + mattressH*0.2, -d/2 + headboardD + d*0.1]} radius={0.05} castShadow receiveShadow>
                {getMaterial(color, 'fabric')}
            </RoundedBox>
            <RoundedBox args={[w*0.35, mattressH*0.4, d*0.15]} position={[w*0.25, frameH + mattressH + mattressH*0.2, -d/2 + headboardD + d*0.1]} radius={0.05} castShadow receiveShadow>
                {getMaterial(color, 'fabric')}
            </RoundedBox>
        </group>
    );
};

// ---- WARDROBE ----
const Wardrobe3D = ({ w, d, h, color }) => {
    return (
        <group>
            <mesh position={[0, h/2, 0]} castShadow receiveShadow>
                <boxGeometry args={[w, h, d]} />
                {getMaterial(color, 'wood')}
            </mesh>
            {/* Doors */}
            <mesh position={[-w/4 - 0.01, h/2, d/2 + 0.01]} castShadow receiveShadow>
                <boxGeometry args={[w/2 - 0.04, h*0.95, 0.02]} />
                {getMaterial(color, 'wood')}
            </mesh>
            <mesh position={[w/4 + 0.01, h/2, d/2 + 0.01]} castShadow receiveShadow>
                <boxGeometry args={[w/2 - 0.04, h*0.95, 0.02]} />
                {getMaterial(color, 'wood')}
            </mesh>
            {/* Handles */}
            <mesh position={[-0.05, h/2, d/2 + 0.03]} castShadow receiveShadow>
                <boxGeometry args={[0.02, h*0.15, 0.02]} />
                {getMaterial('#aaaaaa', 'metal')}
            </mesh>
            <mesh position={[0.05, h/2, d/2 + 0.03]} castShadow receiveShadow>
                <boxGeometry args={[0.02, h*0.15, 0.02]} />
                {getMaterial('#aaaaaa', 'metal')}
            </mesh>
        </group>
    );
};

// ---- BOOKSHELF ----
const Bookshelf3D = ({ w, d, h, color }) => {
    const sideW = w * 0.06;
    const shelfCount = 5;
    const shelfH = h * 0.02;
    const shelves = [];
    for(let i = 0; i < shelfCount; i++){
        shelves.push(
            <mesh key={i} position={[0, (h / (shelfCount - 1)) * i + shelfH/2, 0]} castShadow receiveShadow>
                <boxGeometry args={[w - sideW*2, shelfH, d]} />
                {getMaterial(color, 'wood')}
            </mesh>
        );
    }
    return (
        <group>
            <mesh position={[-w/2 + sideW/2, h/2, 0]} castShadow receiveShadow>
                <boxGeometry args={[sideW, h, d]} />
                {getMaterial(color, 'wood')}
            </mesh>
            <mesh position={[w/2 - sideW/2, h/2, 0]} castShadow receiveShadow>
                <boxGeometry args={[sideW, h, d]} />
                {getMaterial(color, 'wood')}
            </mesh>
            <mesh position={[0, h/2, -d/2 + d*0.04]} castShadow receiveShadow>
                <boxGeometry args={[w - sideW*2, h, d*0.08]} />
                {getMaterial(color, 'wood')}
            </mesh>
            {shelves}
        </group>
    );
};

// ---- LAMP ----
const Lamp3D = ({ w, d, h, color }) => {
    const baseSize = w * 0.5;
    const poleH = h * 0.65;
    const poleW = 0.05;
    const shadeW = w * 0.7;
    const shadeH = h * 0.3;
    return (
        <group>
            {/* Base */}
            <mesh position={[0, 0.02, 0]} castShadow receiveShadow>
                <cylinderGeometry args={[baseSize/2, baseSize/2, 0.04, 32]} />
                {getMaterial('#50505A', 'metal')}
            </mesh>
            {/* Pole */}
            <mesh position={[0, poleH/2 + 0.04, 0]} castShadow receiveShadow>
                <cylinderGeometry args={[poleW/2, poleW/2, poleH, 16]} />
                {getMaterial('#aaa', 'metal')}
            </mesh>
            {/* Shade */}
            <mesh position={[0, poleH + shadeH/2 + 0.04, 0]} castShadow receiveShadow>
                <cylinderGeometry args={[shadeW/2.5, shadeW/2, shadeH, 32, 1, true]} />
                {getMaterial(color, 'fabric')}
            </mesh>
            {/* Light bulb inner */}
            <mesh position={[0, poleH + shadeH/2, 0]}>
                <sphereGeometry args={[0.08, 16, 16]} />
                <meshStandardMaterial color="#fff" emissive="#ffea00" emissiveIntensity={2} />
            </mesh>
            <pointLight position={[0, poleH + shadeH/2, 0]} intensity={1} distance={3} color="#ffea00" />
        </group>
    );
};

// ---- TV UNIT ----
const TVUnit3D = ({ w, d, h, color }) => {
    const baseH = h * 0.55;
    const tvW = w * 0.85;
    const tvH = h * 0.45;
    const tvD = 0.05;
    return (
        <group>
            {/* Cabinet Base */}
            <mesh position={[0, baseH/2, 0]} castShadow receiveShadow>
                <boxGeometry args={[w, baseH, d]} />
                {getMaterial(color, 'wood')}
            </mesh>
            {/* Screen */}
            <mesh position={[0, baseH + tvH/2 + 0.05, 0]} castShadow receiveShadow>
                <boxGeometry args={[tvW, tvH, tvD]} />
                {getMaterial('#222', 'metal')}
            </mesh>
            <mesh position={[0, baseH + tvH/2 + 0.05, tvD/2 + 0.001]}>
                <planeGeometry args={[tvW*0.95, tvH*0.9]} />
                {getMaterial('#111', 'screen')}
            </mesh>
        </group>
    );
};

// ---- DESK ----
const Desk3D = ({ w, d, h, color }) => {
    const topH = h * 0.05;
    const legH = h - topH;
    const legW = w * 0.06;
    const legD = d * 0.06;
    const drawerW = w * 0.35;
    return (
        <group>
            <mesh position={[0, legH + topH/2, 0]} castShadow receiveShadow>
                <boxGeometry args={[w, topH, d]} />
                {getMaterial(color, 'wood')}
            </mesh>
            {/* Legs */}
            <mesh position={[-w/2 + legW, legH/2, -d/2 + legD]} castShadow receiveShadow>
                <boxGeometry args={[legW, legH, legD]} />
                {getMaterial(color, 'wood')}
            </mesh>
            <mesh position={[-w/2 + legW, legH/2, d/2 - legD]} castShadow receiveShadow>
                <boxGeometry args={[legW, legH, legD]} />
                {getMaterial(color, 'wood')}
            </mesh>
            {/* Drawer */}
            <mesh position={[w/2 - drawerW/2 - 0.05, legH/2, 0]} castShadow receiveShadow>
                <boxGeometry args={[drawerW, legH, d*0.9]} />
                {getMaterial(color, 'wood')}
            </mesh>
        </group>
    );
};

// ---- BEANBAG ----
const Beanbag3D = ({ w, d, h, color }) => {
    return (
        <group>
            {/* Main squashed body */}
            <mesh position={[0, h * 0.4, 0]} scale={[1, h/w * 0.8, d/w]} castShadow receiveShadow>
                <sphereGeometry args={[w/2, 32, 16]} />
                {getMaterial(color, 'fabric')}
            </mesh>
            {/* Top pinch / wrinkle */}
            <mesh position={[0, h * 0.8, 0]} castShadow receiveShadow>
                <coneGeometry args={[w * 0.15, h * 0.4, 16]} />
                {getMaterial(color, 'fabric')}
            </mesh>
        </group>
    );
};

// ---- MIRROR ----
const Mirror3D = ({ w, d, h, color, name }) => {
    const isRound = name && name.toLowerCase().includes('round');
    const frameDepth = Math.max(0.02, d); // Frames are usually thin
    
    return (
        <group>
            {isRound ? (
                // Round Mirror
                <group position={[0, h/2, 0]} rotation={[Math.PI / 2, 0, 0]}>
                    {/* Frame */}
                    <mesh castShadow receiveShadow>
                        <cylinderGeometry args={[w/2, w/2, frameDepth, 32]} />
                        {getMaterial(color, 'metal')}
                    </mesh>
                    {/* Glass Reflective Surface */}
                    <mesh position={[0, frameDepth/2 + 0.001, 0]}>
                        <cylinderGeometry args={[w/2 - 0.03, w/2 - 0.03, 0.01, 32]} />
                        {getMaterial('#fff', 'mirror_glass')}
                    </mesh>
                </group>
            ) : (
                // Standing / Rectangular Mirror
                <group position={[0, h/2, 0]}>
                    {/* Frame / Backing */}
                    <mesh castShadow receiveShadow>
                        <boxGeometry args={[w, h, frameDepth]} />
                        {getMaterial(color, 'wood')}
                    </mesh>
                    {/* Glass Reflective Surface */}
                    <mesh position={[0, 0, frameDepth/2 + 0.001]}>
                        <planeGeometry args={[w - 0.06, h - 0.06]} />
                        {getMaterial('#fff', 'mirror_glass')}
                    </mesh>
                </group>
            )}
        </group>
    );
};

// ---- CUSTOM (Generic nicely styled box for unrecognized types) ----
const Custom3D = ({ w, d, h, color, name }) => {
    return (
        <group position={[0, h/2, 0]}>
            <RoundedBox args={[w, h, d]} radius={0.02} smoothness={4} castShadow receiveShadow>
                {getMaterial(color, 'wood')}
            </RoundedBox>
            {/* Add a subtle inset line around the top/bottom for architectural detail */}
            <mesh position={[0, h/2 - 0.05, 0]}>
                <boxGeometry args={[w + 0.005, 0.01, d + 0.005]} />
                {getMaterial('#222', 'metal')}
            </mesh>
            <mesh position={[0, -h/2 + 0.05, 0]}>
                <boxGeometry args={[w + 0.005, 0.01, d + 0.005]} />
                {getMaterial('#222', 'metal')}
            </mesh>
        </group>
    );
};

const FurnitureMesh = ({ item, offsetX, offsetZ }) => {
    const w = item.width, d = item.depth, h = item.height;
    const x = item.x - offsetX + w/2;
    const z = item.y - offsetZ + d/2;
    const color = item.color || '#cccccc';

    let content = null;
    const isBeanbag = item.name && item.name.toLowerCase().includes('beanbag');

    if (isBeanbag) {
        content = <Beanbag3D w={w} d={d} h={h} color={color} />;
    } else {
        switch (item.category) {
            case 'SOFA': content = <Sofa3D w={w} d={d} h={h} color={color} />; break;
            case 'BED': content = <Bed3D w={w} d={d} h={h} color={color} />; break;
            case 'CHAIR': content = <Chair3D w={w} d={d} h={h} color={color} />; break;
            case 'DINING_TABLE':
            case 'COFFEE_TABLE':
            case 'SIDE_TABLE': content = <Table3D w={w} d={d} h={h} color={color} />; break;
            case 'WARDROBE': content = <Wardrobe3D w={w} d={d} h={h} color={color} />; break;
            case 'BOOKSHELF': content = <Bookshelf3D w={w} d={d} h={h} color={color} />; break;
            case 'DESK': content = <Desk3D w={w} d={d} h={h} color={color} />; break;
            case 'LAMP': content = <Lamp3D w={w} d={d} h={h} color={color} />; break;
            case 'TV_UNIT': content = <TVUnit3D w={w} d={d} h={h} color={color} />; break;
            case 'MIRROR': content = <Mirror3D w={w} d={d} h={h} color={color} name={item.name} />; break;
            case 'RUG': 
                content = (
                    <mesh position={[0, 0.01, 0]} receiveShadow>
                        <boxGeometry args={[w, 0.02, d]} />
                        {getMaterial(color, 'fabric')}
                    </mesh>
                ); 
                break;
            default:
                content = <Custom3D w={w} d={d} h={h} color={color} name={item.name} />;
        }
    }

    return (
        <group position={[x, 0, z]} rotation={[0, THREE.MathUtils.degToRad(-item.rotation || 0), 0]}>
            {content}
        </group>
    );
};

const Scene = ({ design }) => {
    const room = design.room;
    if (!room) return null;

    const offsetX = room.width / 2;
    const offsetZ = room.depth / 2;

    const floorColor = room.floorColor || '#8C7A6B';
    const wallColor = room.wallColor || '#E8E8E8';

    return (
        <group>
            {/* Floor */}
            <mesh position={[0, 0, 0]} rotation={[-Math.PI / 2, 0, 0]} receiveShadow>
                <planeGeometry args={[room.width, room.depth]} />
                <meshStandardMaterial color={floorColor} roughness={0.6} metalness={0.05} />
            </mesh>

            {/* Back Wall */}
            <mesh position={[0, room.height / 2, -room.depth / 2]} receiveShadow castShadow>
                <boxGeometry args={[room.width, room.height, 0.2]} />
                <meshStandardMaterial color={wallColor} roughness={0.9} />
            </mesh>

            {/* Left Wall */}
            <mesh position={[-room.width / 2, room.height / 2, 0]} receiveShadow castShadow>
                <boxGeometry args={[0.2, room.height, room.depth]} />
                <meshStandardMaterial color={wallColor} roughness={0.9} />
            </mesh>

            {/* Furniture Elements */}
            {design.furnitureItems && design.furnitureItems.map((item, idx) => (
                <FurnitureMesh key={item.id || idx} item={item} offsetX={offsetX} offsetZ={offsetZ} />
            ))}
        </group>
    );
};

export default Scene;
