<!DOCTYPE html>
<html>
    <head>
        <title>Upload form</title>
    </head>
    <body>
        <h1>Simple file upload form</h1>
        <form action="upload.php" method="post" enctype="multipart/form-data">
            <label for="file"><span>Filename:</span></label>
            <input type="file" name="file" id="file" /> 
            <br/>
            <input type="submit" name="submit" value="Submit" />
        </form>
    </body>
</html>