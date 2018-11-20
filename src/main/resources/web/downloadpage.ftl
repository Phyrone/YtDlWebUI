<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Youtube Converter</title>
    <link rel="stylesheet" href="assets/css/style.css">
    <link rel="stylesheet" href="assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="assets/css/fontawesome.min.css">
    <link rel="prefetch" href="assets/img/turntable-1109588_1920.jpg">
    <script src="assets/js/JQuerry.js"></script>
    <script src="assets/js/PropperJS.js"></script>
    <script src="assets/js/download.js"></script>
    <script src="assets/js/bootstrap.min.js"></script>
    <script src="assets/js/script.js"></script>
</head>
<body class="background">
<div style="margin: 10% 5%">
    <div class="center">
        <div class="downloadBox">
            <div class="row">
                <div class="col-md-10">
                    <form id="download-form" action="javascript:onDownloadRequest('${defaultprofilename}')">
                        <input type="url" id="urlInput" class="disabled" required placeholder="VideoURL"
                               style="width: 100%">
                    </form>
                </div>
                <div class="col-md-2">
                    <div class="btn-group">
                        <button type="button" id="normalDownloadBTM"
                                onclick="onDownloadRequest('${defaultprofilename}')"
                                class="btn  btn-block btn-outline-primary disabled">Download
                        </button>
                        <button type="submit" form="download-form"
                                id="advDownloadBTM"
                                class="btn btn-outline-primary dropdown-toggle dropdown-toggle-split disabled"
                                data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                            <span class="sr-only">Toggle Dropdown</span>
                        </button>
                        <div class="dropdown-menu">
                        ${profilesDropdown}
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
            <tbody id="downloads-table">
            </tbody>
        </table>
    </div>
</div>
</body>
</html>