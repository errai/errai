function initMenu() {
    $("div > dl dd").hide();
    $("div dl dt:has(.chapter)").click(
            function(event) {
                $(this).find("a").attr('href', '#');
                $(this).next().slideToggle('fast');
                event.stopImmediatePropagation();
            }
    );

}
$(document).ready(function() {
    initMenu();
});