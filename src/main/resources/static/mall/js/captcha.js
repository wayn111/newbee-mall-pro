let start = 0;
let startY = 0;
let currentCaptchaId = null;
let movePercent = 0;
const bgImgWidth = $(".bg-img-div").width();
let end = 206;
let startSlidingTime;
let entSlidingTime;
const trackArr = [];
let that = this;

$(function () {
    refreshCaptcha();
});

$(".slider-move-btn").mousedown((event) => {
    console.log("bb")
    startSlidingTime = new Date();
    start = event.pageX;
    startY = event.pageY;
    $(".slider-move-btn").css("background-position", "-5px 31.0092%")
    window.addEventListener("mousemove", move);
    window.addEventListener("mouseup", up);
});

$(".slider-move-btn").on("touchstart", (event) => {
    console.log("aa")
    startSlidingTime = new Date();
    start = event.pageX;
    startY = event.pageY;
    if (start === undefined) {
        start = event.originalEvent.targetTouches[0].pageX
        startY = event.originalEvent.targetTouches[0].pageY;
    }
    $(".slider-move-btn").css("background-position", "-5px 31.0092%")
    window.addEventListener("touchmove", move);
    window.addEventListener("touchend", up);
});

function move(event) {
    if (event instanceof TouchEvent) {
        event = event.touches[0];
    }
    let moveX = event.pageX - start;
    let pageX = event.pageX;
    let pageY = event.pageY;
    console.log("x:", pageX, "y:", pageY, "time:", new Date().getTime() - startSlidingTime.getTime());
    trackArr.push({x: pageX - start, y: pageY - startY, t: (new Date().getTime() - startSlidingTime.getTime())});
    if (moveX < 0) {
        moveX = 0;
    } else if (moveX > end) {
        moveX = end;
    }
    // if (moveX > 0 && moveX <= end) {
    $(".slider-move-btn").css("transform", "translate(" + moveX + "px, 0px)")
    $(".slider-img-div").css("transform", "translate(" + moveX + "px, 0px)")
    // }
    movePercent = moveX / bgImgWidth;
}

function up(event) {
    entSlidingTime = new Date();
    console.log(currentCaptchaId, movePercent, bgImgWidth);
    window.removeEventListener("mousemove", move);
    window.removeEventListener("mouseup", up);
    valid();
}


$(".close-btn").click(() => {
    hideCaptcha();
});

$(".refresh-btn").click(() => {
    refreshCaptcha();
});

function showCaptcha() {
    $(".captcha-modal").show();
}

function hideCaptcha() {
    refreshCaptcha();
    $(".captcha-modal").hide();
}

function valid() {

    console.log("=======================")
    console.log("startTime", startSlidingTime);
    console.log("endTime", entSlidingTime);
    console.log("track", JSON.stringify(trackArr));


    let data = {
        bgImageWidth: bgImgWidth,
        bgImageHeight: $(".slider-img-div").height(),
        sliderImageWidth: $(".slider-img-div").width(),
        sliderImageHeight: $(".slider-img-div").height(),
        startSlidingTime: startSlidingTime,
        entSlidingTime: entSlidingTime,
        trackList: trackArr
    };

    $.ajax({
        type: "POST",
        url: _ctx + "tianai/check?id=" + currentCaptchaId,
        contentType: "application/json", //必须这样写
        dataType: "json",
        data: JSON.stringify(data),//schoolList是你要提交是json字符串
        success: function (res) {
            console.log(res);
            if (res) {
                that.login();
            } else {
                refreshCaptcha();
            }

        }

    })
}

function refreshCaptcha() {
    $.get(_ctx + "tianai/gen", function (data) {
        reset();
        currentCaptchaId = data.id;
        $("#bg-img").attr("src", data.captcha.backgroundImage);
        $("#slider-img").attr("src", data.captcha.sliderImage);
    })
}

function reset() {
    $(".slider-move-btn").css("background-position", "-5px 11.79625%")
    $(".slider-move-btn").css("transform", "translate(0px, 0px)")
    $(".slider-img-div").css("transform", "translate(0px, 0px)")
    start = 0;
    startSlidingTime = null;
    entSlidingTime = null;
    trackArr.length = 0;
    movePercent = 0;
    currentCaptchaId = null;
    startY = 0;
}
