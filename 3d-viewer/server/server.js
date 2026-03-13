const express = require('express');
const mysql = require('mysql2/promise');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());

// Create connection pool
const pool = mysql.createPool({
  host: process.env.DB_HOST || 'localhost',
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || 'root',
  database: process.env.DB_NAME || 'furniturevision',
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0
});

// Test connection
pool.getConnection()
    .then(conn => {
        console.log('MySQL Connected successfully');
        conn.release();
    })
    .catch(err => {
        console.error('MySQL connection error:', err);
    });

// API to GET a design by ID
app.get('/api/designs/:id', async (req, res) => {
    try {
        const [designs] = await pool.query(
            `SELECT d.id, d.designer_id, d.customer_name, d.notes, 
                    r.name as room_name, r.width as room_width, r.depth as room_depth, r.height as room_height, r.shape as room_shape, 
                    r.wall_color, r.floor_color, r.ceiling_color
             FROM designs d 
             JOIN rooms r ON d.room_id = r.id 
             WHERE d.id = ?`, 
             [req.params.id]
        );

        if (designs.length === 0) {
            return res.status(404).json({ error: 'Design not found' });
        }
        
        const row = designs[0];
        
        // Fetch furniture items
        const [items] = await pool.query(
            "SELECT * FROM placed_furniture WHERE design_id = ?",
            [req.params.id]
        );
        
        // Format to match old structure
        const designData = {
            id: row.id,
            designerId: row.designer_id,
            customerName: row.customer_name,
            notes: row.notes,
            room: {
                name: row.room_name,
                width: row.room_width,
                depth: row.room_depth,
                height: row.room_height,
                shape: row.room_shape,
                wallColor: row.wall_color,
                floorColor: row.floor_color,
                ceilingColor: row.ceiling_color
            },
            furnitureItems: items.map(i => ({
                id: i.id,
                name: i.name,
                category: i.category,
                width: i.width,
                depth: i.depth,
                height: i.height,
                x: i.x,
                y: i.y,
                rotation: i.rotation,
                scale: i.scale_val,
                color: i.color
            }))
        };
        
        res.json(designData);
    } catch (err) {
        console.error('MySQL query error:', err);
        res.status(500).json({ error: err.message });
    }
});

const PORT = process.env.PORT || 3001;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
