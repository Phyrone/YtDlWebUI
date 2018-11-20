const wsUrl = "ws://" + window.location.host + "/" + location.pathname + "/api/ws";
const regex = /(http|https):\/\/(\w+:?\w*)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%!\-\/]))?/;
console.log("Ws-Url: " + wsUrl);
websocket = new WebSocket(wsUrl);


websocket.onopen = function (ev) {
    wsReady = true;
    console.log("Websocket Connected!");
    maybeReady();
};
let ready = false;
let wsReady = false;
let pageReady = false;
let normalDownloadBTM;
websocket.onmessage = function (e) {
    if (!ready) return;
    console.log("WS-Message: \"" + e.data+"\"");
    let packet = JSON.parse(e.data.toString());
    switch (packet.id) {
        case 32:

            break;
        case 30:

            break;
    }

};
async function updateVideo(id,percent){

}

$(document).ready(function () {
    normalDownloadBTM = document.getElementById("normalDownloadBTM");
    pageReady = true;
    console.log("Page Ready!");
    maybeReady();
});


function maybeReady() {
    if (wsReady && pageReady) {
        onReady();
    }
}


/**
 * @return {boolean}
 */
function validURL(str) {
    return regex.test(str);
}

async function onReady() {
    ready = true;
    normalDownloadBTM.classList.remove("disabled");
    normalDownloadBTM.onclick = function () {
        document.getElementById("download-form").submit();
    };
    $("#download-form").submit(function () {
        onDownloadRequest()
    })
}

function onDownloadRequest() {
    let url = document.getElementById("urlInput").value;
    if (validURL(url)) {
        requestDownload(url)
    } else {
        alert("Please enter a valid URL!")
    }
}

function requestDownload(downloadLink) {
    sendJson(new DownloadRequestJson(downloadLink))
}

function sendJson(jsonClass) {
    websocket.send(JSON.stringify(jsonClass))
}

class DownloadRequestJson {

    constructor(url) {
        this.url = url;
        this.id = 31;
    }
}