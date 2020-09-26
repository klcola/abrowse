ABrowse.browse.TrackRequest = function (locations, trackConfig) {

    this.blockRequests = locations;
    this.trackGroupName = trackConfig.trackGroupName;
    this.trackName = trackConfig.trackName;
    this.viewName = trackConfig.viewName;
    this.yIndex = trackConfig.yIndex;
};

ABrowse.browse.BrowseRequest = function (genomeDB, chrName, trackRequestArray) {
    this.genome = genomeDB;
    this.chrName = chrName;
    this.trackRequests = trackRequestArray;
};

ABrowse.browse.StatusPanel = function (config) {

    this.genomeBrowser = config.genomeBrowser;
    this.id = config.id;
    this.top = config.top;
    this.left = config.left;
    this.width = config.width;
    this.height = config.height;
    this.initializePanel();
};

ABrowse.browse.StatusPanel.prototype.initializePanel = function () {

    this.containerDiv = document.getElementById(this.id);
    if (!this.containerDiv) {
        this.containerDiv = document.createElement("div");
        this.containerDiv.id = this.id;
        this.genomeBrowser.browserDiv.appendChild(this.containerDiv);
    }

    this.containerDiv.style.top = this.top + "px";
    this.containerDiv.style.left = this.left + "px";
    this.containerDiv.style.width = this.width + "px";
    this.containerDiv.style.height = this.height + "px";
    this.containerDiv.style.position = "absolute";

};

ABrowse.browse.GenomeBrowser = function (config) {

    this.genomeDB = null;

    this.id = config.id ? config.id : "browser";
    this.searchInputId = config.searchInputId ? config.searchInputId : "position";
    this.speciesSelectId = config.speciesSelectId ? config.speciesSelectId : "species_adv";
    this.canvasHeaderScaleHeight = config.canvasHeaderScaleHeight ? config.canvasHeaderScaleHeight : 20;
    this.canvasHeaderScaleFontSize = config.canvasHeaderScaleFontSize ? config.canvasHeaderScaleFontSize : 10;
    this.trackSpacing = config.trackSpacing ? config.trackSpacing : 2;
    this.mouseWheelSpeed = config.mouseWheelSpeed ? config.mouseWheelSpeed : 5;

    this.top = config.top;
    this.left = config.left;
    this.width = config.width;
    this.height = config.height;

    //this.chrThumbHeight = config.chrThumbHeight;
    //if (!this.chrThumbHeight) {
    this.chrThumbTop = this.top;
    this.chrThumbLeft = this.left;
    this.chrThumbHeight = 30;
    //}

    //this.thumbCanvasSpacing = config.thumbCanvasSpacing;
    //if (!this.thumbCanvasSpacing) {
    this.thumbCanvasSpacing = 5;
    //}

    this.canvasTop = this.top + this.chrThumbHeight + this.thumbCanvasSpacing;
    this.canvasLeft = this.left;
    this.canvasHeight = this.height - this.chrThumbHeight - this.thumbCanvasSpacing;
    if (this.width >= 1440) {
        this.canvasWidth = Math.round(this.width * 0.85);
    } else if (this.width >= 1280) {
        this.canvasWidth = Math.round(this.width * 0.8);
    } else {
        this.canvasWidth = Math.round(this.width * 0.7);
    }
    var numOf100 = Math.round(this.canvasWidth / 100);
    this.canvasWidth = 100 * numOf100;
    this.chrThumbWidth = this.canvasWidth;

    var maxDisplayedChrLocLength = this.canvasWidth * 1000;
    this.maxLocationRadius = maxDisplayedChrLocLength / 2;

    this.statusPanelTop = this.canvasTop;
    this.statusPanelLeft = this.canvasLeft + this.canvasWidth + 10;
    this.statusPanelHeight = this.canvasHeight - 5;
    this.statusPanelWidth = this.width - this.canvasWidth - 20;

    this.slideStepInPixels = config.slideStepInPixels;
    this.isBasepairView = config.isBasepairView;

    this.viewableLoc = config.viewableLoc;
    this.svgDisplayScale = (this.imageWidth / this.viewableLoc.length()).toFixed(3);
    this.slidedDistanceInPixels = 0;
    this.inserting = false;
    this.trackConfigs = {};
    this.canvasId = [this.id, "canvas"].join("-");
    this.canvasFrameId = [this.id, "canvasframe"].join("-");
    this.canvasBackgroundId = [this.id, "canvasbackground"].join("-");
    this.chrThumbId = [this.id, "chr_thumb"].join("-");
    this.statusPanelId = config.statusPanelId;

    this.trackOption = new ABrowse.option.TrackOption(this);

    this.abrowseMatrixE = 0;
    this.abrowseMatrixF = this.canvasHeaderScaleHeight + 10;

    this.requestIndex = 0; // 请求次数

    this.initializeBrowserLayout();
    this.initializeCanvas();

    localStorage.trackData = JSON.stringify({});
};

ABrowse.browse.GenomeBrowser.prototype.initializeBrowserLayout = function () {

    this.browserDiv = document.getElementById(this.id);
    if (!this.browserDiv) {
        this.browserDiv = document.createElement("div");
        this.browserDiv.id = this.id;
        document.body.appendChild(this.browserDiv);
    }

    this.chrThumbSvg = document.getElementById(this.chrThumbId);  // canvas brush 白条部分
    if (!this.chrThumbSvg) {
        this.chrThumbSvg = document.createElementNS(ABrowse.SVG_NS, "svg");
        this.chrThumbSvg.id = this.chrThumbId;
        this.chrThumbSvg.setAttribute("xmlns", "http://www.w3.org/2000/svg");
        this.browserDiv.appendChild(this.chrThumbSvg);
    }
    this.chrThumbSvg.style.top = this.chrThumbTop + "px";
    this.chrThumbSvg.style.left = this.chrThumbLeft + "px";
    this.chrThumbSvg.setAttribute("width", this.chrThumbWidth);
    this.chrThumbSvg.setAttribute("height", this.chrThumbHeight);
    this.chrThumbSvg.style.position = "absolute";
    this.chrThumbSvg.style.border = "1px solid grey";

    /*
    $("#progressbar").offset({
        top: this.chrThumbTop,
        left: this.chrThumbLeft
    }).width(this.chrThumbWidth).height(this.chrThumbHeight);
    */

    this.canvasFrameDiv = document.getElementById(this.canvasFrameId);
    if (!this.canvasFrameDiv) {
        this.canvasFrameDiv = document.createElement("div");
        this.canvasFrameDiv.id = this.canvasFrameId;
        this.browserDiv.appendChild(this.canvasFrameDiv);
    }
    this.canvasFrameDiv.style.top = this.canvasTop + "px";
    this.canvasFrameDiv.style.left = this.canvasLeft + "px";
    this.canvasFrameDiv.style.width = this.canvasWidth + "px";
    this.canvasFrameDiv.style.height = this.canvasHeight + "px";
    this.canvasFrameDiv.style.position = "absolute";
    this.canvasFrameDiv.style.border = "1px solid grey";
    this.canvasFrameDiv.style.cursor = "move";

    this.canvasBackgroundSvg = document.getElementById(this.canvasBackgroundId);
    if (!this.canvasBackgroundSvg) {
        this.canvasBackgroundSvg = document.createElementNS(ABrowse.SVG_NS, "svg");
        this.canvasBackgroundSvg.id = this.canvasBackgroundId;
        this.canvasBackgroundSvg.setAttribute("xmlns", "http://www.w3.org/2000/svg");
        this.canvasFrameDiv.appendChild(this.canvasBackgroundSvg);
    }
    this.canvasBackgroundSvg.style.top = "0px";
    this.canvasBackgroundSvg.style.left = "0px";
    // this.canvasBackgroundSvg.setAttribute("width", this.canvasWidth);
    this.canvasBackgroundSvg.setAttribute("width", '100%');
    this.canvasBackgroundSvg.setAttribute("height", this.canvasHeight);
    this.canvasBackgroundSvg.style.position = "absolute";
    //this.canvasBackgroundSvg.style.border = "1px solid grey";

    this.canvasSvg = document.getElementById(this.canvasId);
    if (!this.canvasSvg) {
        this.canvasSvg = document.createElementNS(ABrowse.SVG_NS, "svg");
        this.canvasSvg.id = this.canvasId;
        this.canvasSvg.setAttribute("xmlns", "http://www.w3.org/2000/svg");
        this.canvasFrameDiv.appendChild(this.canvasSvg);
    }
    // this.canvasSvg.style.top = "0px";
    this.canvasSvg.style.top = "20px";
    this.canvasSvg.style.left = "0px";
    // this.canvasSvg.setAttribute("width", this.canvasWidth);
    this.canvasSvg.setAttribute("width", '100%');
    this.canvasSvg.setAttribute("height", this.canvasHeight);
    this.canvasSvg.style.position = "absolute";
    //this.canvasSvg.style.border = "1px solid grey";
    //this.canvasSvg.style.cursor = "url('/css/hand.cur'), move";

    $("#" + this.chrThumbId).on("mousedown","g", {browser: this}, ABrowse.browse.brushOnMouseDown)
    $("#" + this.chrThumbId).on("mousemove", {browser: this}, ABrowse.browse.brushOnMouseMove)
    $("#" + this.chrThumbId).on("mouseup",   {browser: this}, ABrowse.browse.brushOnMouseUp)

    $("#" + this.canvasFrameId).on("mousemove", {browser: this}, ABrowse.browse.brushOnMouseMove)
    $("#" + this.canvasFrameId).on("mouseup",   {browser: this}, ABrowse.browse.brushOnMouseUp)

    $("#hideIcon").on("click", {browser: this}, ABrowse.browse.canvasWidthChange)    //   侧边栏收起，染色体位置重新渲染
    $("#showIcon").on("click", {browser: this}, ABrowse.browse.canvasWidthChange)    //   侧边栏展开，染色体位置重新渲染

    $("#" + this.canvasFrameId).bind('mousedown', {browser: this}, ABrowse.browse.canvasOnMouseDown)
        .mousemove({browser: this}, ABrowse.browse.canvasOnMouseMove)
        .mouseup({browser: this}, ABrowse.browse.canvasOnMouseUp);

    $("#" + this.canvasFrameId).bind('mousewheel', {browser: this}, function(event) {
        event.preventDefault();
        var deltaX = event.originalEvent.deltaX;
        var deltaY = event.originalEvent.deltaY;

        var distanceX = event.data.browser.mouseWheelSpeed * deltaX;
        var distanceY = event.data.browser.mouseWheelSpeed * deltaY * (-1); // mouseWheelSpeed: 5
        var genomeBrowser = event.data.browser;

        genomeBrowser.move(distanceX, distanceY);
    });

    //  方向键控制 svg 的移动
    $(window).bind('keydown', {browser: this}, function (event) {
      if (event.target.nodeName !== 'INPUT') { // 此判断为了使 input 能正常输入
        var distanceX, distanceY
        var genomeBrowser = event.data.browser;
        var keyID = event.keyCode ? event.keyCode :event.which
        if (keyID === 37) { // left
          distanceX = -5
          distanceY = 0
          genomeBrowser.move(distanceX, distanceY);
        } else if (keyID === 38) { // top
          distanceX = 0
          distanceY = 5
          genomeBrowser.move(distanceX, distanceY);
        } else if (keyID === 39) { // right
          distanceX = 5
          distanceY = 0
          genomeBrowser.move(distanceX, distanceY);
        } else if (keyID === 40) { // down
          distanceX = 0
          distanceY = -5
          genomeBrowser.move(distanceX, distanceY);
        }
      }
    });

    if (this.statusPanelId) {
        this.statusPanel = new ABrowse.browse.StatusPanel(
            {
                genomeBrowser: this,
                id: this.statusPanelId,
                top: this.statusPanelTop,
                left: this.statusPanelLeft,
                width: this.statusPanelWidth,
                height: this.statusPanelHeight
            }
        );
    }

};

ABrowse.browse.GenomeBrowser.prototype.initializeCanvas = function () {

    while (this.canvasSvg.firstChild) {
        this.canvasSvg.removeChild(this.canvasSvg.firstChild);
    }

    while (this.canvasBackgroundSvg.firstChild) {
        this.canvasBackgroundSvg.removeChild(this.canvasBackgroundSvg.firstChild);
    }

    this.abrowseMatrixE = 0;
    this.abrowseMatrixF = this.canvasHeaderScaleHeight + 10;

    var verticalScaleLineNum = this.width / 100;
    var canvasHeight = this.canvasHeight;
    var y1 = this.canvasHeaderScaleHeight.toString();
    var markLineGroup = document.createElementNS(ABrowse.SVG_NS, "g");
    this.canvasBackgroundSvg.appendChild(markLineGroup);
    for (var i = 0; i < verticalScaleLineNum; ++i) {
        var x = 100 * i;
        if (i != 0) {

            var markLine = document.createElementNS(ABrowse.SVG_NS, "polyline");
            var pointsArray = [];
            pointsArray.push([x, y1].join(","));
            pointsArray.push([x, canvasHeight].join(","));
            pointsArray.push([x + 1, canvasHeight].join(","));
            pointsArray.push([x + 1, y1].join(","));
            markLine.setAttribute("points", pointsArray.join(" "));
            markLine.setAttribute("fill", "rgb(190,190,190)");
            markLineGroup.appendChild(markLine);
        }
        for (var n = 1; n < 10; ++n) {
            var x_ = x + n * 10;
            var subMarkLine = document.createElementNS(ABrowse.SVG_NS, "polyline");
            var subPointsArray = [];
            subPointsArray.push([x_, y1].join(","));
            subPointsArray.push([x_, canvasHeight].join(","));
            subPointsArray.push([x_ + 1, canvasHeight].join(","));
            subPointsArray.push([x_ + 1, y1].join(","));
            subMarkLine.setAttribute("points", subPointsArray.join(" "));
            subMarkLine.setAttribute("fill", "rgb(230,230,230)");
            markLineGroup.appendChild(subMarkLine);
        }
    }

    this.headerMarksSvgGroup = document.createElementNS(ABrowse.SVG_NS, "g");
    this.canvasBackgroundSvg.appendChild(this.headerMarksSvgGroup);
};

ABrowse.browse.GenomeBrowser.prototype.drawHorizontalScaleMarks = function (location) { // 绘制 数字 header
    var blockSvgGroupId = ABrowse.view.createBlockSvgGroupId("canvasHeaderScale", location.start, location.end); // set element id eg: 'canvasHeaderScale: start - end'
    var blockSvgGroup = document.getElementById(blockSvgGroupId);
    if (!blockSvgGroup) {
        blockSvgGroup = document.createElementNS(ABrowse.SVG_NS, "g");
        blockSvgGroup.id = blockSvgGroupId;
        blockSvgGroup.__abrowse__start = parseInt(location.start) - this.originalPointX;
        blockSvgGroup.__abrowse__end = parseInt(location.end) - this.originalPointX;
        blockSvgGroup.__abrowse__max_level = 1;
    } else {
        return null;
    }

    var marksNum = this.canvasWidth / 100;  // svg width / 100
    var unitBasepairNum = (location.length() / marksNum).toFixed(0); // (end - start) / marksNum
    var start = location.start;
    var fontSize = (this.canvasHeaderScaleFontSize / this.svgDisplayScale).toFixed(0);  // canvasHeaderScaleFontSize: 10
    for (var i = 0; i < marksNum; i++) {
        var text = document.createElementNS(ABrowse.SVG_NS, "text");
        var markStart = start + unitBasepairNum * i - this.originalPointX;
        text.textContent = (markStart + this.originalPointX).toString();
        var fontY = (15 / this.svgDisplayScale).toFixed(0);

        text.setAttribute("x", markStart.toString());
        text.setAttribute("y", fontY.toString());
        text.setAttribute("transform", ["scale(1,", this.svgDisplayScale, ")"].join(""));
        text.setAttribute("font-size", fontSize + "px");
        text.setAttribute("font-family", "Calibri, Arial, Helvetica, sans-serif");
        text.setAttribute("font-weight", "bold");
        text.setAttribute("fill", "black");
        blockSvgGroup.appendChild(text);
    }

    ABrowse.view.insertBlockSvgGroup(this.headerMarksSvgGroup, blockSvgGroup);
};

ABrowse.browse.GenomeBrowser.prototype.canvasScaleMarkSlide = function (slideStepInPixels) {

    var a = this.svgDisplayScale;
    var b = 0;
    var c = 0;
    var d = 1;
    var e = this.headerMarksSvgGroup.__abrowse__matrix_e - slideStepInPixels;
    this.headerMarksSvgGroup.__abrowse__matrix_e = e;
    var f = 0;

    this.headerMarksSvgGroup.setAttribute("transform", ABrowse.view.createTransformMatrix(a, b, c, d, e, f));
};

ABrowse.browse.GenomeBrowser.prototype.drawCurrentAreaRect = function () {  //  画当前区域占整个染色体的位置 height:30

  var currentAreaSvgGroup = document.getElementById("currentAreaSvgGroup");
  var currentAreaRect = document.getElementById("currentAreaRect");

  var xDist = (this.viewableLoc.start / (this.chrLength / this.chrThumbWidth)).toFixed(0) - 0

  var width = ((this.viewableLoc.end - this.viewableLoc.start) / (this.chrLength / this.chrThumbWidth))

  if ((xDist + width) >= this.chrThumbWidth - 2) {  //  滑块滑到最右边的时候
    xDist = this.chrThumbWidth - width - 2
  }

  if (!currentAreaSvgGroup) {

    var currentAreaSvgGroup = document.createElementNS(ABrowse.SVG_NS, "g");  //  brush rect svg group
    currentAreaSvgGroup.id = "currentAreaSvgGroup";
    currentAreaSvgGroup.style.cursor = "n-resize";

    currentAreaSvgGroup.setAttribute("transform", ABrowse.view.createTransformMatrix(1, 0, 0, 1, xDist, 0));


    currentAreaRect = document.createElementNS(ABrowse.SVG_NS, "rect");
    currentAreaRect.id = "currentAreaRect";
    currentAreaRect.setAttribute("width", width);
    currentAreaRect.setAttribute("height", "30");
    currentAreaRect.setAttribute("fill", "#ffb092");
    currentAreaRect.setAttribute("stroke", "#ff4b13");

    currentAreaSvgGroup.appendChild(currentAreaRect)

    this.chrThumbSvg.appendChild(currentAreaSvgGroup);

  } else {

    currentAreaRect.setAttribute("width", width);

    currentAreaSvgGroup.setAttribute("transform", ABrowse.view.createTransformMatrix(1, 0, 0, 1, xDist, 0));
  }

  this.currentAreaSvgGroup = currentAreaSvgGroup
  this.currentAreaSvgGroup._brush_matrix_e = xDist
  this.currentAreaSvgGroup._brush_width = width

}

ABrowse.browse.GenomeBrowser.prototype.brushMove = function (xDist) {

  var x = this.currentAreaSvgGroup._brush_matrix_e - xDist

  if (x <= 0) {  //  滑块滑到最左边的时候
    x = 0
  }
  if ((x + this.currentAreaSvgGroup._brush_width) >= this.chrThumbWidth - 2) {  //  滑块滑到最右边的时候
    x = this.chrThumbWidth - this.currentAreaSvgGroup._brush_width - 2
  }

  this.currentAreaSvgGroup._brush_matrix_e = x

  this.currentAreaSvgGroup.setAttribute("transform", ABrowse.view.createTransformMatrix(1, 0, 0, 1, this.currentAreaSvgGroup._brush_matrix_e, 0));
}

ABrowse.browse.GenomeBrowser.prototype.brushMoveEnd = function () {

  // window.clearTimeout(this.timer)

  var scale = this.chrThumbWidth / this.chrLength

  var start = (this.currentAreaSvgGroup._brush_matrix_e / scale).toFixed(0)
  var end = ((this.currentAreaSvgGroup._brush_matrix_e + this.currentAreaSvgGroup._brush_width) / scale).toFixed(0)

  var value = [this.chrName, [start, end].join("-")].join(":");

  $("#" + this.searchInputId).val(value)

  // this.timer = setTimeout(() => {
    this.submit()
  // }, 0)

}

ABrowse.browse.GenomeBrowser.prototype.changeTrackView = function (trackName, newViewName) {

    var top = this.abrowseMatrixF;

    var trackAlreadyOpened = false;
    var trackView;
    for (var n = 0; n < this.canvasSvg.childNodes.length;) {
        var trackSvgGroup = this.canvasSvg.childNodes[n];

        if (trackSvgGroup.__abrowse__is_track) {

            if (trackSvgGroup.__abrowse__trackName != trackName) {
                this.setTop(trackSvgGroup, top);
                top += trackSvgGroup.__abrowse__height;
                ++n;
            }
            else {
              this.canvasSvg.removeChild(trackSvgGroup);
                // trackAlreadyOpened = true;
                trackView = ABrowse.trackViewsMap[newViewName];
                if (trackView) {
                  // console.log(2);
                  //   var trackResponse = JSON.parse(localStorage.trackData)[trackName];
                  //   this.canvasSvg.removeChild(trackSvgGroup);
                  //
                  //   var trackSvgGroupComplexus = trackView.render(trackResponse, top);
                  //
                  //   this.canvasSvg.insertBefore(trackSvgGroupComplexus.trackSvgGroup, this.canvasSvg.childNodes[n]);
                  //
                  //   // this.canvasSvg.replaceChild(trackSvgGroupComplexus.trackSvgGroup, trackSvgGroup);
                  //   top += trackSvgGroupComplexus.trackSvgGroup.__abrowse__height + this.trackSpacing;
                  //   ++n;
                }
                else {
                    // this.canvasSvg.removeChild(trackSvgGroup);
                    let trackData = JSON.parse(localStorage.trackData)
                    trackData[trackName] = undefined
                    // localStorage.trackData = JSON.stringify(trackData)
                    // should not increase n here
                }
            }
        } else {
            ++n;
        }
    }

    this.trackOption.updateTrackConfigs();

    if (!trackAlreadyOpened) {
        trackView = ABrowse.trackViewsMap[newViewName];
        if (trackView) {
            var browseRequest = this.getTrackSwitchedOnBrowseRequest(trackName);
            console.log("DEBUG - changeTrackView - browseRequest:" + JSON.stringify(browseRequest));
            $.ajax({
                type: "POST",
                url: "/gmap/browse",
                data: {
                    browse_request: JSON.stringify(browseRequest),
                    requestIndex: this.requestIndex
                },
                success: this.simpleRequestSuccess,
                error: this.simpleRequestFailure,
                dataType: "json",
                context: this
            });
        }
    }
};

ABrowse.browse.GenomeBrowser.prototype.setTop = function (trackSvgGroup, top) {

    trackSvgGroup.__abrowse__matrix_f = top;

    var trackHeaderSvgGroup = trackSvgGroup.childNodes[0];
    var trackBodySvgGroup = trackSvgGroup.childNodes[1];
    var trackBodyMarginTop = trackBodySvgGroup.__abrowse__matrix_f - trackHeaderSvgGroup.__abrowse__matrix_f;

    trackHeaderSvgGroup.setAttribute("transform", ABrowse.view.createTransformMatrix(1, 0, 0, 1, 0, top));
    trackHeaderSvgGroup.__abrowse__matrix_f = top;

    var a = this.svgDisplayScale;
    var b = 0;
    var c = 0;
    var d = 1;
    var e = this.abrowseMatrixE;
    /*
     if (trackBodySvgGroup.__abrowse__matrix_e) {
     e = trackBodySvgGroup.__abrowse__matrix_e;
     } else {
     trackBodySvgGroup.__abrowse__matrix_e = e;
     }
     */

    var f = top + trackBodyMarginTop;
    trackBodySvgGroup.__abrowse__matrix_f = f;

    trackBodySvgGroup.setAttribute("transform", ABrowse.view.createTransformMatrix(a, b, c, d, e, f));

};

ABrowse.browse.GenomeBrowser.prototype.simpleRequestSuccess = function (response, textStatus, jqXHR) {

    if (response.requestIndex < this.requestIndex) {
      return
    }

    /*
    if (this.progressBarInitialized) {
        $("#progressbar").progressbar("destroy");
        this.progressBarInitialized = false;
    }
    */

    var browseResponse = response; // 请求返回的源数据

    this.chrLength = response.chrLength //  染色体总长度

    this.drawCurrentAreaRect()

    if (browseResponse.isError) {
        console.log("DEBUG ERROR - browseResponse:" + JSON.stringify(browseResponse));
    }

    if (browseResponse.error) {
        this.inserting = false;
        ABrowse.showErrorInfo(browseResponse.message);
        return;
    }

    var top = this.abrowseMatrixF; // 30
    for (var n = 0; n < this.canvasSvg.childNodes.length; ++n) {
        var trackSvgGroup = this.canvasSvg.childNodes[n];
        if (trackSvgGroup.__abrowse__is_track) {
            top += trackSvgGroup.__abrowse__height;
        }
    }

    var trackResponses = browseResponse.trackResponses;


    for (var idx = 0; idx < trackResponses.length; ++idx) {
        var trackResponse = trackResponses[idx];

        var blockResponses = trackResponse.blockResponses; // [{}, {}, {}]

        for (var n = 0; n < blockResponses.length; ++n) {
            this.drawHorizontalScaleMarks(new ABrowse.Location(blockResponses[n].start, blockResponses[n].end));  // 坐标刻度
        }

        var storedTrackResponse = JSON.parse(localStorage.trackData)[trackResponse.trackName];
        if (storedTrackResponse) {
            var storedBlockResponses = storedTrackResponse.blockResponses;

            if (storedBlockResponses[0].start < blockResponses[0].start) {
                for (var n = 0; n < blockResponses.length; ++n) {
                    storedBlockResponses.push(blockResponses[n]);
                }
            } else {
                for (var n = 0; n < blockResponses.length; ++n) {
                    storedBlockResponses.unshift(blockResponses[n]);
                }
            }
        } else {
            let trackData = JSON.parse(localStorage.trackData)
            trackData[trackResponse.trackName] = trackResponse
            // localStorage.trackData = JSON.stringify(trackData)
        }

        var trackViewClass = trackResponse.viewName;
        var trackView = ABrowse.trackViewsMap[trackViewClass];
        var trackSvgGroupComplexus = trackView.render(trackResponse, top);
        if (trackSvgGroupComplexus.initialCreated) {
            this.canvasSvg.appendChild(trackSvgGroupComplexus.trackSvgGroup);
        }
        top += trackSvgGroupComplexus.trackSvgGroup.__abrowse__height + this.trackSpacing;
    }

    this.inserting = false;
};

ABrowse.browse.GenomeBrowser.prototype.simpleRequestFailure = function (response, textStatus, jqXHR) {
    this.inserting = false;

    ABrowse.showErrorInfo("Sorry, there are errors detected, which maybe caused by the network transport. Please resubmit your request or reload the page.");
};

ABrowse.browse.GenomeBrowser.prototype.submit = function () {

    this.initializeCanvas();

    var searchStr = $("#" + this.searchInputId).get(0).value; // input 的 value eg:chr1:12166808-12210688
    var genomeDB = $("#" + this.speciesSelectId + " option:selected").attr("value"); // select 的 value eg: human
    console.log("DEBUG: searchStr: " + searchStr);
    var chrLoc = ABrowse.parsePositionStr(searchStr); // 将 searchStr 格式化成 json
    this.realSubmit(genomeDB, chrLoc);

};

ABrowse.browse.GenomeBrowser.prototype.zoomInSubmit = function () {
    this.initializeCanvas();
    var searchStr = $("#" + this.searchInputId).get(0).value;
    var genomeDB = $("#" + this.speciesSelectId + " option:selected").attr("value");
    console.log("DEBUG: searchStr: " + searchStr);
    var chrLoc = ABrowse.parsePositionStr(searchStr);
    chrLoc.zoomin();
    this.viewableLoc.start = chrLoc.start;
    this.viewableLoc.end = chrLoc.end;
    this.updateInputField();
    this.realSubmit(genomeDB, chrLoc);
};

ABrowse.browse.GenomeBrowser.prototype.zoomOutSubmit = function () {

    this.initializeCanvas();
    var searchStr = $("#" + this.searchInputId).get(0).value;
    var genomeDB = $("#" + this.speciesSelectId + " option:selected").attr("value");
    console.log("DEBUG: searchStr: " + searchStr);
    var chrLoc = ABrowse.parsePositionStr(searchStr);
    chrLoc.zoomout(this.maxLocationRadius);
    this.viewableLoc.start = chrLoc.start;
    this.viewableLoc.end = chrLoc.end;
    this.updateInputField();
    this.realSubmit(genomeDB, chrLoc);
};

ABrowse.browse.GenomeBrowser.prototype.realSubmit = function (genomeDB, chromosomeLoc) {
    this.requestIndex++

    // if (this.inserting) {
    //     return;
    // }

    this.inserting = true;

    var s2 = chromosomeLoc.start;
    var e2 = chromosomeLoc.end;

    this.originalPointX = chromosomeLoc.start;
    this.viewableLoc = new ABrowse.Location(s2, e2); // {end: , start: }

    var requestLength = chromosomeLoc.end - chromosomeLoc.start; // end - start
    this.svgDisplayScale = (this.canvasWidth / requestLength).toFixed(3); // svg width / (end - start) 得到比例尺

    this.genomeDB = genomeDB; // eg: human
    this.chrName = chromosomeLoc.chr; // eg: chr1

    this.trackConfigs[this.genomeDB] = this.trackOption.updateTrackConfigs();
    localStorage.trackData = JSON.stringify({});


    var s1 = s2 - requestLength;
    var e1 = s2;

    var s3 = e2;
    var e3 = s3 + requestLength;

    var blockRequests = [
        {start: s1, end: e1},
        {start: s2, end: e2},
        {start: s3, end: e3}
    ];

    var a = this.svgDisplayScale; // svg wdith / (end - start)
    var b = 0;
    var c = 0;
    var d = 1;
    var e = 0;
    var f = 0;
    this.headerMarksSvgGroup.__abrowse__matrix_e = e;
    this.headerMarksSvgGroup.setAttribute("transform", ABrowse.view.createTransformMatrix(a, b, c, d, e, f)); // 带数字的表头部分 一共有 3段

    var trackRequests = [];
    for (var n = 0; n < this.trackConfigs[this.genomeDB].length; ++n) {
        var trackConfig = this.trackConfigs[this.genomeDB][n];
        if ("off" != trackConfig.viewName) {
            trackRequests.push(
                {
                    trackGroupName: trackConfig.trackGroupName,
                    trackName: trackConfig.trackName,
                    viewName: trackConfig.viewName,
                    yIndex: trackConfig.yIndex,
                    blockRequests: blockRequests
                }
            );
        }
    }

    var browseRequest = {
        genome: genomeDB,
        chrName: this.chrName,
        trackRequests: trackRequests
    };

    // console.log("DEBUG: browseRequest: " + JSON.stringify(browseRequest));

    $.ajax({
        type: "POST",
        url: "/gmap/browse",
        data: {
            browse_request: JSON.stringify(browseRequest),
            requestIndex: this.requestIndex
        },
        success: this.simpleRequestSuccess,
        error: this.simpleRequestFailure,
        dataType: "json",
        context: this
    });

    //this.progressBarInitialized = true;

};

ABrowse.browse.GenomeBrowser.prototype.getTrackSwitchedOnBrowseRequest = function (trackName) {

    var searchStr = $("#" + this.searchInputId).get(0).value;
    var chrLoc = ABrowse.parsePositionStr(searchStr);


    var requestedLocs = [];
    var location;

    if (this.canvasSvg.childNodes.length > 0) {
        var trackBodySvgGroup;
        for (var n = 0; n < this.canvasSvg.childNodes.length; ++n) {
            var trackSvgGroup = this.canvasSvg.childNodes[n];
            if (trackSvgGroup.__abrowse__is_track) {
                trackBodySvgGroup = trackSvgGroup.childNodes[1];
                break;
            }
        }

        for (var n = 0; n < trackBodySvgGroup.childNodes.length; ++n) {
            if (this.viewableLoc.isIncludingPoint(trackBodySvgGroup.childNodes[n].__abrowse__start) ||
                this.viewableLoc.isIncludingPoint(trackBodySvgGroup.childNodes[n].__abrowse__end)) {
                location = new ABrowse.Location(trackBodySvgGroup.childNodes[n].__abrowse__start,
                    trackBodySvgGroup.childNodes[n].__abrowse__end);
                break;
            }
        }

    } else {
        location = this.viewableLoc;
    }

    requestedLocs.push(location.createPrevLocation());
    requestedLocs.push(location);
    requestedLocs.push(location.createNextLocation());
    var trackConfig = this.trackOption.getTrackConfig(trackName);
    var trackRequest = new ABrowse.browse.TrackRequest(requestedLocs, trackConfig);
    return new ABrowse.browse.BrowseRequest(this.genomeDB, chrLoc.chr, [trackRequest]);
};

ABrowse.browse.GenomeBrowser.prototype.getSlideRightBrowseRequest = function () {
    var trackConfigs = this.trackConfigs[this.genomeDB];
    var trackRequests = [];
    for (var idx = 0; idx < trackConfigs.length; ++idx) {
        var trackConfig = trackConfigs[idx];
        if (trackConfig.viewName != "off") {
            var trackBodySvgGroup = document.getElementById(ABrowse.view.createTrackBodySvgGroupId(trackConfig.trackName));
            var leftmostBlockSvgGroup = trackBodySvgGroup.childNodes[0];
            var leftmostStart = leftmostBlockSvgGroup.__abrowse__start;
            var leftmostEnd = leftmostBlockSvgGroup.__abrowse__end;
            var requestedLength = leftmostEnd - leftmostStart;
            var requestedLocs = [];
            if (Math.abs(leftmostStart - this.viewableLoc.start) / requestedLength < 2) {
                var location = new ABrowse.Location(leftmostStart - requestedLength,
                    leftmostEnd - requestedLength);
                requestedLocs.push(location);
                //this.drawHorizontalScaleMarks(location);
            }
            if (requestedLocs.length > 0) {
                trackRequests.push(new ABrowse.browse.TrackRequest(requestedLocs, trackConfig));
            }
        }
    }

    if (trackRequests.length > 0) {
        return new ABrowse.browse.BrowseRequest(this.genomeDB, this.chrName, trackRequests);
    } else {
        return null;
    }
};

ABrowse.browse.GenomeBrowser.prototype.getSlideLeftBrowseRequest = function () {

    var trackConfigs = this.trackConfigs[this.genomeDB];
    var trackRequests = [];

    for (var idx = 0; idx < trackConfigs.length; ++idx) {
        var trackConfig = trackConfigs[idx];
        if (trackConfig.viewName != "off") {
            var trackBodySvgGroup = document.getElementById(ABrowse.view.createTrackBodySvgGroupId(trackConfig.trackName));
            var blocksNum = trackBodySvgGroup.childNodes.length;
            var rightmostBlockSvgGroup = trackBodySvgGroup.childNodes[blocksNum - 1];
            var rightmostStart = rightmostBlockSvgGroup.__abrowse__start;
            var rightmostEnd = rightmostBlockSvgGroup.__abrowse__end;
            var requestedLength = rightmostEnd - rightmostStart;
            var requestedLocs = [];
            if (Math.abs(rightmostStart - this.viewableLoc.start) / requestedLength < 2) {
                var location = new ABrowse.Location(rightmostEnd,
                    rightmostEnd + requestedLength);
                requestedLocs.push(location);
                //this.drawHorizontalScaleMarks(location);
            }
            if (requestedLocs.length > 0) {
                trackRequests.push(new ABrowse.browse.TrackRequest(requestedLocs, trackConfig));
            }
        }
    }

    if (trackRequests.length > 0) {
        return new ABrowse.browse.BrowseRequest(this.genomeDB, this.chrName, trackRequests);
    } else {
        return null;
    }
};

ABrowse.browse.GenomeBrowser.prototype.updateInputField = function () {

    var input = document.getElementById(this.searchInputId);
    var realStart = this.viewableLoc.start;
    var realEnd = this.viewableLoc.end;
    input.value = [this.chrName, [realStart, realEnd].join("-")].join(":");
};

/*
 slide left: direction = 1;
 slide right: direction = -1;
 */
ABrowse.browse.GenomeBrowser.prototype.slide = function (direction) {

    var xDist = this.slideStepInPixels * direction;
    this.move(xDist, 0);
};


ABrowse.browse.GenomeBrowser.prototype.move = function (xDist, yDist) {

    if (0 == this.canvasSvg.childNodes.length) {
        return;
    }

    var slideDistanceInBasePair = Math.round(xDist / this.svgDisplayScale); //  input start 变化的差值

    if (xDist <= 0 && this.viewableLoc.start + slideDistanceInBasePair <= 0) {  //  滑倒最左边时 (min)
      slideDistanceInBasePair = - this.viewableLoc.start  //  当 start <= 0 时，input 不再变化
      xDist = slideDistanceInBasePair * this.svgDisplayScale
    }

    if (xDist >= 0 && this.viewableLoc.end + slideDistanceInBasePair >= this.chrLength) {  //  滑倒最右边时 (max)
      slideDistanceInBasePair = (this.chrLength - (this.viewableLoc.end - this.viewableLoc.start)) - this.viewableLoc.start  //  当 start >= chrLength 时，input 不再变化
      xDist = slideDistanceInBasePair * this.svgDisplayScale
    }

    this.abrowseMatrixE -= xDist;

    this.viewableLoc.slide(slideDistanceInBasePair);
    this.updateInputField();
    this.drawCurrentAreaRect(); //  更新当前区域占整个染色体位置的 rect
    this.canvasScaleMarkSlide(xDist);
    var top = this.abrowseMatrixF - yDist;

    let canvasHeight = this.canvasHeight
    let allHeight = 0
    for (var i = 0; i < this.canvasSvg.childNodes.length; ++i) {
        var trackSvgGroup = this.canvasSvg.childNodes[i]
        if (trackSvgGroup.__abrowse__is_track) {
          allHeight += trackSvgGroup.__abrowse__height
        }
    }

    if (top < 0) { // 向上拖动 判断最后一个 track 的高度是否大于 canvasSvg 的高度
      if (this.canvasSvg.childNodes[this.canvasSvg.childNodes.length - 1].__abrowse__height <= canvasHeight) {
        if (Math.abs(top) > allHeight - 30 - this.canvasSvg.childNodes[this.canvasSvg.childNodes.length - 1].__abrowse__height) {
          top = - (allHeight - 30 - this.canvasSvg.childNodes[this.canvasSvg.childNodes.length - 1].__abrowse__height)
        }
      } else {
        if (Math.abs(top) > allHeight - 30 - canvasHeight) {
          top = - (allHeight - 30 - canvasHeight) - 50
        }
      }
    }

    if (top > 0) { // 向下拖动 判断第一个 track 的高度是否大于 canvasSvg 的高度
      if (this.canvasSvg.childNodes[0].__abrowse__height <= canvasHeight) {
        if (top >= canvasHeight - this.canvasSvg.childNodes[0].__abrowse__height + 30) {
          top = canvasHeight - this.canvasSvg.childNodes[0].__abrowse__height + 30
        }
      } else {
        if (top >= canvasHeight - 50) {
          top = canvasHeight - 50
        }
      }
    }

    this.abrowseMatrixF = top;

    for (var i = 0; i < this.canvasSvg.childNodes.length; ++i) {
        var trackSvgGroup = this.canvasSvg.childNodes[i];
        if (trackSvgGroup.__abrowse__is_track) {
            trackSvgGroup.__abrowse__trackView.slide(trackSvgGroup, xDist, top);
            top += trackSvgGroup.__abrowse__height + this.trackSpacing;
        }
    }

    this.slidedDistanceInPixels -= xDist;
    // if (!this.inserting && Math.abs(this.slidedDistanceInPixels) >= this.canvasWidth / 2.5) {
    if (Math.abs(this.slidedDistanceInPixels) >= this.canvasWidth / 2.5) {
        this.inserting = true;

        var browseRequest = null;
        if (xDist < 0) {

            browseRequest = this.getSlideRightBrowseRequest();
        } else {
            browseRequest = this.getSlideLeftBrowseRequest();
        }

        if (browseRequest) {

            $.ajax({
                type: "POST",
                url: "/gmap/browse",
                data: {
                    browse_request: JSON.stringify(browseRequest),
                    requestIndex: this.requestIndex
                },
                success: this.simpleRequestSuccess,
                error: this.simpleRequestFailure,
                dataType: "json",
                context: this
            });

        } else {
            this.inserting = false;
        }

        this.slidedDistanceInPixels = 0;
    }
};

ABrowse.browse.brushOnMouseDown = function (event) {
    var genomeBrowser = event.data.browser;
    genomeBrowser.brush_dragable = true;
    genomeBrowser.dragStartX = event.pageX;
    genomeBrowser.chrThumbSvg.style.cursor = "n-resize";

};

ABrowse.browse.brushOnMouseMove = function (event) {
    var genomeBrowser = event.data.browser;
    if (genomeBrowser.brush_dragable) {
        var xDist = genomeBrowser.dragStartX - event.pageX;
        genomeBrowser.dragStartX = event.pageX;
        genomeBrowser.brushMove(xDist);
        genomeBrowser.chrThumbSvg.style.cursor = "n-resize";
    }
};

ABrowse.browse.brushOnMouseUp = function (event) {
    var genomeBrowser = event.data.browser;
    if (genomeBrowser.brush_dragable) {
      genomeBrowser.brush_dragable = false;
      genomeBrowser.brushMoveEnd()
      genomeBrowser.chrThumbSvg.style.cursor = "default";
    }
};

ABrowse.browse.canvasOnMouseDown = function (event) {
    var genomeBrowser = event.data.browser;
    genomeBrowser.dragable = true;
    //genomeBrowser.canvasFrameDiv.style.cursor = "url('/css/clutch.cur'), move";
    genomeBrowser.dragStartX = event.pageX;
    genomeBrowser.dragStartY = event.pageY;
};

ABrowse.browse.canvasOnMouseMove = function (event) {
    var genomeBrowser = event.data.browser;
    if (genomeBrowser.dragable) {
        //genomeBrowser.canvasFrameDiv.style.cursor = "url('/css/clutch.cur'), move";
        var xDist = genomeBrowser.dragStartX - event.pageX;
        var yDist = genomeBrowser.dragStartY - event.pageY;
        genomeBrowser.dragStartX = event.pageX;
        genomeBrowser.dragStartY = event.pageY;
        genomeBrowser.move(xDist, yDist);
    }
};

ABrowse.browse.canvasOnMouseUp = function (event) {
    var genomeBrowser = event.data.browser;
    genomeBrowser.dragable = false;
    //genomeBrowser.canvasFrameDiv.style.cursor = "url('/css/hand.cur'), move";
};

ABrowse.browse.canvasWidthChange = function (event) {
  var genomeBrowser = event.data.browser;
  let width = $("#browser-canvas").width();  // 侧边栏展开或收起时，画布的 width
  genomeBrowser.chrThumbWidth = width
  genomeBrowser.drawCurrentAreaRect()
};
