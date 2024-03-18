const toggleSideBar = () => {
    if ($(".sidebar").is(":visible")) {
        // hiding sidebar and moving content to left
        $(".sidebar").css("display", "none");
        $(".content").css("margin-left", "0%");
    } else {
        // displaying sidebar and moving content to right
        $(".sidebar").css("display", "block");
        $(".content").css("margin-left", "20%")
    }

}