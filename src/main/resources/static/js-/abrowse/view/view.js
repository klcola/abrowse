ABrowse.view.entry05CoordinateTransformation = function (entry, ORIGINAL_POINT_X) {

    //var level = entry[0];
    //var entryId = entry[1];
    var start = parseInt(entry[2]);
    entry[2] = start - ORIGINAL_POINT_X;
    var end = parseInt(entry[3]);
    entry[3] = end - ORIGINAL_POINT_X;
    //var strand = entry[4];

};

ABrowse.view.createTransformMatrix = function (a, b, c, d, e, f) {
    var matrix = [a, b, c, d, e, f].join(",");
    return ["matrix(", matrix, ")"].join("");
};

ABrowse.view.createEntryAnchorId = function (trackName, entryId) {
    return [trackName, entryId].join(":");
};

ABrowse.view.createGeneSvgGroupId = function (trackName, geneId) {
    return [trackName, geneId].join(":");
}

ABrowse.view.createBlockSvgGroupId = function (trackName, start, end) {
    return [trackName, [start, end].join("-")].join(":");
};

ABrowse.view.createTrackSvgGroupId = function (trackName) {
    return ["track", trackName].join(":");
};

ABrowse.view.createTrackHeaderSvgGroupId = function (trackName) {
    return ["trackheader", trackName].join(":");
};

ABrowse.view.createTrackBodySvgGroupId = function (trackName) {
    return ["trackbody", trackName].join(":");
};

ABrowse.view.insertBlockSvgGroup = function (trackBodySvgGroup, blockSvgGroup) {

    var children = trackBodySvgGroup.childNodes;
    var blockStart = blockSvgGroup.__abrowse__start;

    if (children.length == 0 || blockStart > children[children.length - 1].__abrowse__start) {
        trackBodySvgGroup.appendChild(blockSvgGroup);
    } else if (blockStart < children[0].__abrowse__start) {
        var originalFirstChild = children[0];
        trackBodySvgGroup.insertBefore(blockSvgGroup, originalFirstChild);
    } else {

        var idx = 0;
        for (; idx < children.length - 1; ++idx) {
            var prevStart = children[idx].__abrowse__start;
            var nextStart = children[idx + 1].__abrowse__start;
            if (prevStart < blockStart && nextStart > blockStart) {
                break;
            }

            var nextBlockSvgGroup = children[idx + 1];
            trackBodySvgGroup.insertBefore(blockSvgGroup, nextBlockSvgGroup);
        }
    }
};

ABrowse.view.View = function (genomeBrowser) {

    this.trackBodyMarginTop = 5;
    this.headerFontSize = 14;
    this.genomeBrowser = genomeBrowser;
    this.colorSchema = ABrowse.view.DefaultColorSchema;
};

ABrowse.view.View.prototype.initializeRender = function (trackResponse) {

    var trackName = trackResponse.trackName;
    var initialCreated = false;
    var trackSvgGroupId = ABrowse.view.createTrackSvgGroupId(trackName);
    var trackSvgGroup = document.getElementById(trackSvgGroupId);

    if (!trackSvgGroup) {
        initialCreated = true;

        trackSvgGroup = document.createElementNS(ABrowse.SVG_NS, "g");
        trackSvgGroup.id = trackSvgGroupId;
        trackSvgGroup.__abrowse__trackName = trackName;
        trackSvgGroup.__abrowse__trackView = this;
        trackSvgGroup.__abrowse__is_track = true;

        var trackHeaderSvgGroupId = ABrowse.view.createTrackHeaderSvgGroupId(trackName);
        var trackHeaderSvgGroup = document.createElementNS(ABrowse.SVG_NS, "g");
        trackHeaderSvgGroup.id = trackHeaderSvgGroupId;

        var text = document.createElementNS(ABrowse.SVG_NS, "text");
        text.textContent = trackResponse.trackDisplayName;
        text.setAttribute("font-size", this.headerFontSize + "px");
        text.setAttribute("font-family", "Calibri, Arial, Helvetica, sans-serif");
        text.setAttribute("font-weight", "bold");
        text.setAttribute("fill", "black");

        trackHeaderSvgGroup.appendChild(text);
        trackSvgGroup.appendChild(trackHeaderSvgGroup);

        var trackBodySvgGroupId = ABrowse.view.createTrackBodySvgGroupId(trackName);
        var trackBodySvgGroup = document.createElementNS(ABrowse.SVG_NS, "g");
        trackBodySvgGroup.id = trackBodySvgGroupId;
        trackSvgGroup.appendChild(trackBodySvgGroup);
    }

    return new ABrowse.view.TrackSvgGroupComplexus(initialCreated, trackSvgGroup);
};

ABrowse.view.View.prototype.insertBlockSvgGroup = ABrowse.view.insertBlockSvgGroup;

ABrowse.view.View.prototype.updateMaxLevel = function (trackBodySvgGroup) {

    var maxLevel = 1;
    for (var i = 0; i < trackBodySvgGroup.childNodes.length; ++i) {
        var blockSvgGroup = trackBodySvgGroup.childNodes[i];
        if (this.genomeBrowser.viewableLoc.isIncludingPoint(blockSvgGroup.__abrowse__start)
            || this.genomeBrowser.viewableLoc.isIncludingPoint(blockSvgGroup.__abrowse__end)) {
            if (maxLevel < blockSvgGroup.__abrowse__max_level) {
                maxLevel = blockSvgGroup.__abrowse__max_level;
            }
        }
    }
    return maxLevel;
};


ABrowse.view.TrackSvgGroupComplexus = function (initialCreated, trackSvgGroup) {
    this.initialCreated = initialCreated;
    this.trackSvgGroup = trackSvgGroup;
};
