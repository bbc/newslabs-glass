<?php

date_default_timezone_set("Europe/London");

const UPLOAD_DIRECTORY = "upload";

// Optionally define a list of allowed extentions and file types
$allowedExtentions = array("jpg", "jpeg", "gif", "png", "mp3", "mp4", "wma", "mov");
$allowedTypes = array("application/octet-stream", "video/mp4", "video/quicktime", "audio/mp3", "audio/wma", "image/pjpeg", "image/gif", "image/jpeg");

try {
    //file_put_contents("log.txt", date('c')." File upload submitted\n", FILE_APPEND);

    if (count($_FILES) == 0)
        throw new Exception("No file supplied.");

    $extension = pathinfo($_FILES['file']['name'], PATHINFO_EXTENSION);

    if (count($allowedExtentions) > 0 && !in_array($_FILES["file"]["type"], $allowedTypes))
        throw new Exception("Invalid file type: ".$_FILES["file"]["type"]);

    if (count($allowedExtentions) > 0 && !in_array($extension, $allowedExtentions))
        throw new Exception("Invalid file extention: ".$extension);

    // Of upload_max_filesize and post_max_size the smallest will be enforced
    if ($_FILES["file"]["size"] > return_bytes(ini_get('upload_max_filesize')) || $_FILES["file"]["size"] > return_bytes(ini_get('post_max_size')))
        throw new Exception("File is too large: ".($_FILES["file"]["size"] / 1024)." Kb");

    if ($_FILES["file"]["error"] > 0)
        throw new Exception("Error during upload: ".$_FILES["file"]["error"]);

    $filename = date('c')."-".$_FILES["file"]["name"];
    if (file_exists($filename))
        throw new Exception("File ".$filename." already exists.");

    if (!is_writable(UPLOAD_DIRECTORY))
        throw new Exception("Do not have permission to write to upload directory.");

    if (!@move_uploaded_file($_FILES["file"]["tmp_name"], UPLOAD_DIRECTORY."/".$filename))
        throw new Exception("Upload failed (check permissions and disk space on upload directory)");

    echo "<h1>File uploaded</h1>";
    echo "Name: ".$_FILES["file"]["name"]."<br />";
    echo "Type: ".$_FILES["file"]["type"]."<br />";
    echo "Size: ".($_FILES["file"]["size"] / 1024)." Kb<br />";
    echo "Temp file: ".$_FILES["file"]["tmp_name"]."<br />";
    echo "Saved in: ".UPLOAD_DIRECTORY."/".$filename."<br />";

} catch (Exception $e) {
    echo "<h1>Error</h1>";
    echo $e->getMessage();
}

function return_bytes($val) {
    $val = trim($val);
    $last = strtolower($val[strlen($val)-1]);
    switch($last) {
        // The 'G' modifier is available since PHP 5.1.0
        case 'g':
            $val *= 1024;
        case 'm':
            $val *= 1024;
        case 'k':
            $val *= 1024;
    }
    return $val;
}
?>