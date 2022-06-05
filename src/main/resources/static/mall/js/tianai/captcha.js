var currentCaptchaConfig;
/** 是否打印日志 */
var isPrintLog = true;

function clearPreventDefault(event) {
    if (event.preventDefault) {
        event.preventDefault();
    }
}

function clearAllPreventDefault($div) {
    $div.each(function (index, el) {
        el.addEventListener('touchmove', clearPreventDefault, false);
    });
}

function reductionAllPreventDefault($div) {
    $div.each(function (index, el) {
        el.removeEventListener('touchmove', clearPreventDefault);
    });
}

function printLog(...params) {
    if (isPrintLog) {
        console.log(JSON.stringify(params));
    }
}

function initConfig(bgImageWidth, bgImageHeight, sliderImageWidth, sliderImageHeight, end) {
    currentCaptchaConfig = {
        startTime: new Date(),
        trackArr: [],
        movePercent: 0,
        bgImageWidth,
        bgImageHeight,
        sliderImageWidth,
        sliderImageHeight,
        end
    }
    printLog("init", currentCaptchaConfig);
    return currentCaptchaConfig;
}

function down(event) {
    let targetTouches = event.originalEvent ? event.originalEvent.targetTouches : event.targetTouches;
    let startX = event.pageX;
    let startY = event.pageY;
    if (startX === undefined) {
        startX = Math.round(targetTouches[0].pageX);
        startY = Math.round(targetTouches[0].pageY);
    }
    currentCaptchaConfig.startX = startX;
    currentCaptchaConfig.startY = startY;

    const pageX = currentCaptchaConfig.startX;
    const pageY = currentCaptchaConfig.startY;
    const startTime = currentCaptchaConfig.startTime;
    const trackArr = currentCaptchaConfig.trackArr;
    trackArr.push({
        x: pageX - startX,
        y: pageY - startY,
        type: "down",
        t: (new Date().getTime() - startTime.getTime())
    });
    printLog("start", startX, startY)
    // pc
    window.addEventListener("mousemove", move);
    window.addEventListener("mouseup", up);
    // 手机端
    window.addEventListener("touchmove", move, false);
    window.addEventListener("touchend", up, false);
    doDown(currentCaptchaConfig);
}

function move(event) {
    if (event instanceof TouchEvent) {
        event = event.touches[0];
    }
    let pageX = Math.round(event.pageX);
    let pageY = Math.round(event.pageY);
    const startX = currentCaptchaConfig.startX;
    const startY = currentCaptchaConfig.startY;
    const startTime = currentCaptchaConfig.startTime;
    const end = currentCaptchaConfig.end;
    const bgImageWidth = currentCaptchaConfig.bgImageWidth;
    const trackArr = currentCaptchaConfig.trackArr;
    let moveX = pageX - startX;
    const track = {
        x: pageX - startX,
        y: pageY - startY,
        type: "move",
        t: (new Date().getTime() - startTime.getTime())
    };
    trackArr.push(track);
    if (moveX < 0) {
        moveX = 0;
    } else if (moveX > end) {
        moveX = end;
    }
    currentCaptchaConfig.moveX = moveX;
    currentCaptchaConfig.movePercent = moveX / bgImageWidth;
    doMove(currentCaptchaConfig);
    printLog("move", track)
}

function up(event) {
    window.removeEventListener("mousemove", move);
    window.removeEventListener("mouseup", up);
    window.removeEventListener("touchmove", move);
    window.removeEventListener("touchend", up);
    if (event instanceof TouchEvent) {
        event = event.changedTouches[0];
    }
    currentCaptchaConfig.stopTime = new Date();
    let pageX = Math.round(event.pageX);
    let pageY = Math.round(event.pageY);
    const startX = currentCaptchaConfig.startX;
    const startY = currentCaptchaConfig.startY;
    const startTime = currentCaptchaConfig.startTime;
    const trackArr = currentCaptchaConfig.trackArr;

    const track = {
        x: pageX - startX,
        y: pageY - startY,
        type: "up",
        t: (new Date().getTime() - startTime.getTime())
    }

    trackArr.push(track);
    printLog("up", track)
    valid(currentCaptchaConfig);
}
