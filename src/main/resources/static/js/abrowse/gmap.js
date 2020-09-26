//ABrowse.HEADER_BOTTOM = 90;
//ABrowse.CHR_OVERVIEW_HEIGHT = 28;
ABrowse.SEP = 3;
//ABrowse.MAP_TOP = ABrowse.HEADER_BOTTOM + ABrowse.CHR_OVERVIEW_HEIGHT + ABrowse.SEP;
//ABrowse.MAP_LEFT = 10;

ABrowse.SVG_NS = "http://www.w3.org/2000/svg";
ABrowse.XLINK_NS = "http://www.w3.org/1999/xlink";

ABrowse.LOGIN_STATUS_SPAN_ID = "account1";

ABrowse.statusTabs = undefined;

ABrowse.defaultGenomeBrowser = undefined;
//new ABrowse.genomebrowser.GenomeBrowser(ABrowse.DEFAULT_GENOME_BROWSER_CONFIG);

//ABrowse.genomesTracksMap = new Object();

ABrowse.trackViewsMap = new Object();

ABrowse.showErrorInfo = function (message) {
    /*
    Ext.MessageBox.show({
        title: "Error Information",
        msg: message,
        buttons: Ext.MessageBox.OK
    });
    */
};

ABrowse.ChromosomeLocation = function (chr, start, end) {
    this.chr = chr;
    this.start = start;
    this.end = end;
};

ABrowse.ChromosomeLocation.prototype.zoomin = function () {
    var center = (this.start + this.end) / 2;
    var radius = (this.end - this.start) / 4;
    this.start = center - radius;
    this.end = center + radius;
};

ABrowse.ChromosomeLocation.prototype.zoomout = function (maxLocRadius) {
    var center = (this.start + this.end) / 2;
    var radius = this.end - this.start;
    if (maxLocRadius && radius > maxLocRadius) {
        radius = maxLocRadius;
    }
    this.start = center - radius;
    this.end = center + radius;
};

ABrowse.Location = function (start, end) {  // [start,end)
    this.start = start;
    this.end = end;
};

ABrowse.Location.prototype.createLocation = function (locStr) {
    var fields = locStr.split('-');
    var start = parseInt(fields[0]);
    var end = parseInt(fields[1]);
    return new ABrowse.Location(start, end);
};

ABrowse.Location.prototype.createPrevLocation = function () {

    var l = this.length();
    var e = this.start;
    var s = e - l;
    return new ABrowse.Location(s, e);
};

ABrowse.Location.prototype.toPrevLocationString = function () {
    var l = this.length();
    var e = this.start;
    var s = e - l;
    return [s, "-", e].join("");
};

ABrowse.Location.prototype.createNextLocation = function () {

    var l = this.length();
    var s = this.end;
    var e = s + l;
    return new ABrowse.Location(s, e);
};

ABrowse.Location.prototype.toNextLocationString = function () {
    var l = this.length();
    var s = this.end;
    var e = s + l;
    return [s, "-", e].join("");
};

ABrowse.Location.prototype.slide = function (slideDistanceInBasePair) {
    this.start = this.start + slideDistanceInBasePair;
    this.end = this.end + slideDistanceInBasePair;
};

ABrowse.Location.prototype.length = function () {
    return this.end - this.start;
};

ABrowse.Location.prototype.createEnlargedLocation = function (magnification, isBasepairView) {

    var half_length = Math.round(this.length() * magnification / 2);
    var middle = Math.round((this.start + this.end) / 2);
    var start = middle - half_length;
    var end = start + half_length * 2;

    if (isBasepairView) {                 //base pair view
        return new ABrowse.Location(start, (start + (GMap.imageWidth / GMap.baseWidthOnImage)));
    } else {                                    // normal view
        return new ABrowse.Location(start, end);
    }
};

ABrowse.Location.prototype.isIncludingPoint = function (x) {
    return this.start <= x && x < this.end;
};

ABrowse.Location.prototype.toString = function () {
    return [this.start, "-", this.end].join("");
};

ABrowse.Location.prototype.clone = function() {
    return new ABrowse.Location(this.start, this.end);
};

ABrowse.TrackConfig = function (trackGroupName, trackName, viewName, yIndex) {
    this.trackGroupName = trackGroupName;
    this.trackName = trackName;
    this.viewName = viewName;
    this.yIndex = yIndex;
};

ABrowse.getViewPort = function () {
    var e = window
        , a = 'inner';
    if (!( 'innerWidth' in window )) {
        a = 'client';
        e = document.documentElement || document.body;
    }
    return { width: e[ a + 'Width' ], height: e[ a + 'Height' ] }
};

ABrowse.mainPageInit = function () {

    //ABrowse.defaultGenomeBrowser = new ABrowse.browse.GenomeBrowser(ABrowse.DEFAULT_GENOME_BROWSER_CONFIG);
    var viewSize = ABrowse.getViewPort();
    var height = viewSize.height;
    var width = viewSize.width;
    console.log("DEBUG - view size:" + width + " x " + height);

    var headerDiv = document.getElementById("header");
    var headerTop = headerDiv.offsetTop;
    var headerHeight = headerDiv.offsetHeight;

    var mapTop = headerTop + headerHeight;
    var mapLeft = 1;
    var mapHeight = height - mapTop - 3;

    var viewableLoc = new ABrowse.Location(9795000, 9807500);

    ABrowse.defaultGenomeBrowser = new ABrowse.browse.GenomeBrowser(
        {
            id: "browser",
            searchInputId: "position",
            speciesSelectId: "species_adv",
            canvasHeaderScaleHeight: 20,
            canvasHeaderScaleFontSize: 10,
            trackSpacing: 0,
            top: mapTop,
            left: mapLeft,
            width: width,
            height: mapHeight,
            slideStepInPixels: 200,
            isBasepairView: false,
            viewableLoc: viewableLoc,
            statusPanelId: "statuspanel"
        }
    );

    ABrowse.viewsInit(ABrowse.defaultGenomeBrowser);

    /*
    if ($.browser.msie) {
        ABrowse.getMouseXY = ABrowse.getIEMouseXY;
        ABrowse.isMouseLeftButton = ABrowse.isIEMouseLeftButton;
        ABrowse.isMouseRightButton = ABrowse.isIEMouseRightButton;
    } else {
        ABrowse.getMouseXY = ABrowse.getGeckoMouseXY;
        ABrowse.isMouseLeftButton = ABrowse.isGeckoMouseLeftButton;
        ABrowse.isMouseRightButton = ABrowse.isGeckoMouseRightButton;
    }
    */

};

ABrowse.viewsInit = function (genomeBrowser) {

    ABrowse.trackViewsMap["GeneModelLikeView"]
        = new ABrowse.view.GeneModelLikeView(genomeBrowser);

    /*
    ABrowse.trackViewsMap["WiggleDataView"]
        = new ABrowse.view.WiggleDataView(genomeBrowser);
    */

    ABrowse.trackViewsMap["RNASeqRawDataView"]
        = new ABrowse.view.RNASeqRawDataView(genomeBrowser);

    ABrowse.trackViewsMap["RNASeqSpliceSiteView"]
        = new ABrowse.view.RNASeqSpliceSiteView(genomeBrowser);

    ABrowse.trackViewsMap["BedGraphView"]
        = new ABrowse.view.BedGraphView(genomeBrowser);

    ABrowse.trackViewsMap["GeneModelDenseView"]
        = new ABrowse.view.GeneModelDenseView(genomeBrowser);

    ABrowse.trackViewsMap["StrandBedGraphView"]
        = new ABrowse.view.StrandBedGraphView(genomeBrowser);

    ABrowse.trackViewsMap["HICView"]
        = new ABrowse.view.HICView(genomeBrowser);
};
