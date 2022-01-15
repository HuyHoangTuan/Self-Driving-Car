const express = require('express');
const app = express();

/// view engine
app.set('view engine','ejs');

/// middlewares
app.use(express.static('public'));


app.get('/', (req,res) =>{
    res.render('index')
})

server = app.listen(8080);