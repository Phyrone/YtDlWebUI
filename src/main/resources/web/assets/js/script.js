const dwUrl = "/download";
const isSecrure = window.location.protocol.toLowerCase() === "https:";
const regex = /(http|https):\/\/(\w+:?\w*)?(\S+)(:[0-9]+)?(\/|\/([\w#!:.?+=&%!\-\/]))?/;
const downloadText = "Download";

function getWsProtokol() {
    if (isSecrure)
        return "wss";
    else
        return "ws";
}

const wsUrl = getWsProtokol() + "://" + window.location.host + "/" + location.pathname + "/api/ws";
console.log("Ws-Url: " + wsUrl);
websocket = new WebSocket(wsUrl);


websocket.onopen = function () {
    wsReady = true;
    console.log("Websocket Connected!");
    maybeReady();
};
let ready = false;
let wsReady = false;
let pageReady = false;

websocket.onmessage = function (e) {
    if (!ready) return;
    console.log("WS-Message: \"" + e.data + "\"");
    let packet = JSON.parse(e.data.toString());
    switch (packet.id) {
        case 32:
            updateProgressbarFromPacket(packet);
            break;
        case 30:
            createDownloadItemFromPacket(packet);
            break;
        case 33:
            handlePacket33(packet);
            break;
    }

};

function getProgressbar(id) {
    return "<div class=\"progress\"><div id='pb-" + id + "' class=\"progress-bar progress-bar-striped progress-bar-animated\" role=\"progressbar\" style=\"width:0\" aria-valuenow=\"75\" aria-valuemin=\"0\" aria-valuemax=\"100\"></div></div>"
}

async function updateProgressbarFromPacket(packet) {
    updateProgressbar(packet.videoID, packet.percent)
}

async function updateProgressbar(id, percent) {
    document.getElementById("pb-" + id).style.width = percent + "%"
}

async function createDownloadItemFromPacket(packet) {
    createDownloadItem(packet.videoID, packet.title, packet.url)
}

async function createDownloadItem(id, title, url) {
    console.log("Create DonwloadItem: " + id);
    let e = document.getElementById("downloads-table");
    e.innerHTML = e.innerHTML +
        "<tr id='tr-" + id + "'><td>" + title + "</td>" +
        "<td>" + url + "</td>" +
        "<td id='state-row-" + id + "'>" + getProgressbar(id) + "</td></tr>"
}

async function handlePacket33(packet) {
    switch (packet.state.toUpperCase()) {
        case "FINISH":
            setDownloadBTM(packet.videoID);
            break;
        case "FAILED":
            setDownloadFailed(packet.videoID);
            break;
    }
}

async function setDownloadFailed(id) {
    let element = document.getElementById("tr-" + id);
    element.classList.add("table-danger");
    document.getElementById("state-row-" + id).innerHTML = "FAILED<button type=\"button\" class=\"close\"></button><span aria-hidden=\"true\">&times;</span>"
}

async function setDownloadBTM(id) {
    let element = document.getElementById("state-row-" + id);
    element.innerHTML = "<button class=\"btn btn-block btn-success\" onclick='downloadContent(\"" + id + "\",this)'>" + downloadText + "</button>"
}

async function downloadContent(id, btm) {
    const downloadURL = dwUrl + "?id=" + id;
    console.log("Downloading " + id + "...");
    btm.innerHTML = "<div class=\"lds-facebook\"><div></div><div></div><div></div></div>";
    let infoReq = new XMLHttpRequest();
    infoReq.open("GET", dwUrl + "/info?id=" + id, true);
    infoReq.onload = function (e) {
        let info = JSON.parse(infoReq.response);
        let x = new XMLHttpRequest();
        x.open("GET", downloadURL, true);
        x.responseType = 'blob';
        x.onload = function (e) {
            btm.innerHTML = downloadText;
            download(x.response, info.name, info.type);
        };
        x.send();
    };
    infoReq.send();
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
    document.getElementById("normalDownloadBTM").classList.remove("disabled");
    document.getElementById("advDownloadBTM").classList.remove("disabled");
    $("#download-form").submit(function () {
        onDownloadRequest('AUDIO')
    })
}

function onDownloadRequest(quality) {
    let url = document.getElementById("urlInput").value;
    if (validURL(url)) {
        requestDownload(url, quality)
    } else {
        alert("Please enter a valid URL!")
    }
}

function requestDownload(downloadLink, quality) {
    sendJson(new DownloadRequestJson(downloadLink, quality))
}

function sendJson(jsonClass) {
    websocket.send(JSON.stringify(jsonClass))
}

class DownloadRequestJson {

    constructor(url, quality) {
        this.url = url;
        this.profile = quality;
        this.id = 31;
    }
}