const express = require('express');
const dotenv = require('dotenv');
const errorHandle = require('./middleware/error');
const connectDb = require('./config/db');
dotenv.config({path: './config/config.env'});
const app = express();
const PORT = process.env.PORT;

connectDb();

app.use(express.json());
const contacts = require('./routes/contacts');

app.use(errorHandle);
app.use('/api/v1/covid',contacts);
const server = app.listen(PORT, () => {
    console.log('App listening on port ',PORT);
});

process.on('unhandledRejection',(err,promise) =>{
    console.log(err);
    server.close(()=>process.exit(1));
})