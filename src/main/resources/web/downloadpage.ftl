<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
    <link rel="stylesheet" href="assets/css/style.css">
    <link rel="stylesheet" href="assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="assets/css/fontawesome.min.css">
    <link rel="prefetch" href="assets/img/turntable-1109588_1920.jpg">
    <script src="assets/js/JQuerry.js"></script>
    <script src="assets/js/PropperJS.js"></script>
    <script src="assets/js/bootstrap.min.js"></script>
    <script src="assets/js/script.js"></script>
</head>
<body class="background">
<div style="margin: 10% 5%">
    <div class="center">
        <div class="downloadBox">
            <div class="row">
                <div class="col-md-10">
                    <form id="download-form" action="javascript:onDownloadRequest()">
                        <input type="url" id="urlInput" class="disabled" required placeholder="VideoURL" style="width: 100%">
                    </form>
                </div>
                <div class="col-md-2">
                    <div class="btn-group">
                        <button type="button" id="normalDownloadBTM"
                                class="btn  btn-block btn-outline-primary disabled">Download
                        </button>
                        <button type="submit" form="download-form"
                                class="btn btn-outline-primary dropdown-toggle dropdown-toggle-split disabled"
                                data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                            <span class="sr-only">Toggle Dropdown</span>
                        </button>
                        <div class="dropdown-menu">
                            <a class="dropdown-item" href="#">360p</a>
                            <a class="dropdown-item" href="#">720p</a>
                            <a class="dropdown-item" href="#">1024p</a>
                            <div class="dropdown-divider"></div>
                            <a class="dropdown-item" href="#">Max Quality</a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div>
        <table class="table table-striped table-dark">
            <thead>
            <tr>
                <th scope="col">Name</th>
                <th scope="col">URL</th>
                <th scope="col">Progress/Download</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>Video1</td>
                <td>URL 1</td>
                <td>
                    <div class="progress">
                        <div class="progress-bar progress-bar-striped progress-bar-animated" role="progressbar"
                             style="width: 32%" aria-valuenow="75" aria-valuemin="0"
                             aria-valuemax="100"></div>
                    </div>
                </td>
            </tr>

            <tr>
                <td>Video2</td>
                <td>URL 2</td>
                <th>
                    <button class="btn btn-block btn-success">Download</i></button>
                </th>
            </tr>
            <tr class="table-danger">
                <td>VIDEO</td>
                <td>URL</td>
                <td>FAILED
                    <button type="button" class="close"></button>
                    <span aria-hidden="true">&times;</span>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>