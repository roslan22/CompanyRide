
//=======================All onload functions====================
$(function() { // onload...do
    console.log("in onload");
    $("#about").click(showAboutPage);
    $("#movie").click(showMoviePage);
    $("#book").click(showBookPage);
    $("#contacts").click(showContactPage);
	showAboutPage();
});


function clearChangingZone() {
    $("#changing-zone").empty();
}

function retrievePage(urll) {
        console.log("retrieve " + urll);
    clearChangingZone();
    $.ajax({url: urll.toString(),
        success: function(result)
        {
            console.log("retrieve success" );
            $("#changing-zone").append(result);
        },
        error:function(data)
        {
            console.log("retrieve fail " +  data);
        }
    });
}

function showAboutPage() {
    retrievePage("html/about.html");
}

function showMoviePage() {
    retrievePage("html/movie.html");
}

function showContactPage() {
    retrievePage("html/contact.html");
}
function showBookPage() {
    retrievePage("html/projectbook.html");
}
