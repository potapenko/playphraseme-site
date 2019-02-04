'use strict';

var express = require('express');
var conf    = require('./config.json');

var app = express();

var http = require('http').Server(app);
require("./video")(app);

app.use(function(err, req, res, next) {
		res.end(err.message);
});

var server = http.listen(conf.port, function() {
    console.log('Listening on port %d', server.address().port);
});
