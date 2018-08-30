'use strict';

console.log("init video service");

var fs    = require('fs-extra');
var conf  = require('./config.json');

var streamVideoAsFile = function(req, res, streamPath) {
    console.log("show video: " + streamPath);

    if(!fs.existsSync(streamPath)){
        return res.status(404).send('Video Path Not Valid!');
    }

    res.sendFile(streamPath,function (err) {
        if (err) {
            res.status(err.status).end();
        }
    });
    return null;
};

var streamVideoAsStream = function(req, res, streamPath) {
    console.log("show video: " + streamPath);

    if(!fs.existsSync(streamPath)){
        return res.status(404).send('Video Path Not Valid!');
    }

    fs.stat(streamPath, function(err, stat) {
        var total = stat.size, file;

        var contentType = "video/mp4";

        if (req.headers.range) {
            var range = req.headers.range;
            var parts = range.replace(/bytes=/, "").split("-");
            var partialstart = parts[0];
            var partialend = parts[1];

            var start = parseInt(partialstart, 10);
            var end = partialend ? parseInt(partialend, 10) : total - 1;
            var chunksize = (end - start) + 1;

            console.log('video-range: ' + start + ' - ' + end + ' = ' + chunksize);

            file = fs.createReadStream(streamPath, {
                start: start,
                end: end
            });

            res.writeHead(206, {
                'Content-Range': 'bytes ' + start + '-' + end + '/' + total,
                'Accept-Ranges': 'bytes',
                'Content-Length': chunksize,
                'Content-Type': contentType
            });
        } else {
            file = fs.createReadStream(streamPath);
            res.writeHead(200, {
                'Content-Length': total,
                'max-age':  31536000,
                'Content-Type': contentType
            });
        }

        file.on('open', function () {
            res.openedFile = file;
            // file.pipe(brake(1024*50)).pipe(res)
            file.pipe(res);

            res.on('close', function() {
                if (res.openedFile) {
                    res.openedFile.unpipe(this);
                    if (this.openedFile.fd) {
                        fs.close(this.openedFile.fd);
                    }
                }
            });
        });

        file.on('error', function(err) {
            res.end(err);
            console.log("video stream error", err);
        });
    });
    return null;
};

module.exports = function(app) {
    app.get('/:movie/:phrase.mp4', function(req, res) {
        var moviePath = conf.filesFolder + "/" + req.params.movie + "/" + req.params.phrase + ".mp4";
        streamVideoAsStream(req, res, moviePath);
    });
};
