const jsonServer = require('json-server');
const server = jsonServer.create();
const middlewares = jsonServer.defaults();

server.use(middlewares);
server.use(jsonServer.bodyParser);


const emailsPermitidos = new Set([
    'ana.garcia@example.com',
    'luis.fernandez@example.com'
]);

server.get('/customers', (req, res) => {
    const email = String(req.query.email ?? '')
        .trim()
        .toLowerCase();

    if (!email) {
        return res.status(400).json({
            mensaje: "El parámetro 'email' es obligatorio"
        });
    }

    const estado = emailsPermitidos.has(email);

    return res.status(200).json({
        email,
        estado
    });
});

server.post('/customers', async (req, res) => {
    const { nombre, email, telefono } = req.body;

    if (!nombre || !email || !telefono) {
        return res.status(400).json({
            registrado: false,
            mensaje: 'Los campos nombre, email y telefono son obligatorios'
        });
    }

    const emailNormalizado = String(email)
        .trim()
        .toLowerCase();

    await new Promise((resolve) => {
        setTimeout(resolve, 5000);
    });

    console.log(
        `[REGISTRO] Cliente registrado: ` +
        `nombre=${nombre}, email=${emailNormalizado}, telefono=${telefono}`
    );

    return res.status(201).json({
        nombre,
        email: emailNormalizado,
        telefono,
        registrado: true
    });
});

server.use(jsonServer.bodyParser);

server.listen(4200, () => {
	console.log('### Server is running on port 4200 ###');
});
