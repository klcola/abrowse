ABrowse.option.TrackOption = function (genomeBrowser) {

    this.genomeBrowser = genomeBrowser;
};

ABrowse.option.TrackOption.prototype.getTrackConfig = function (trackName) {

    var radioboxes = $(".viewtype:checked[name='" + trackName + "']");
    var viewName = radioboxes[0].value;
    var yIndex = radioboxes[0].title;
    var trackGroupName = radioboxes[0].parentNode.title;
    var trackConfig = new ABrowse.TrackConfig(trackGroupName, trackName, viewName, yIndex);

    return trackConfig;
};

ABrowse.option.TrackOption.prototype.updateTrackConfigs = function () {

    var genomeDB = this.genomeBrowser.genomeDB;

    this.genomeBrowser.trackConfigs[genomeDB] = [];
    var radioboxes = $(".viewtype:checked");

    var yIndex = 0;

    for (var n = 0; n < radioboxes.length; ++n) {
        var radiobox = radioboxes[n];
        var trackGroupName = radiobox.parentNode.title;
        var viewName = radiobox.value;
        var trackName = radiobox.name;
        this.genomeBrowser.trackConfigs[genomeDB].
            push(new ABrowse.TrackConfig(trackGroupName, trackName, viewName, yIndex));
        yIndex ++;
    }

    return this.genomeBrowser.trackConfigs[genomeDB];
};


ABrowse.option.TrackOption.prototype.trackViewOnChecked = function (checkedInput) {

    if (null == this.genomeBrowser.genomeDB) { // eg: human
        return;
    }

    var trackName = checkedInput.name;
    var viewName = checkedInput.value;

    this.genomeBrowser.changeTrackView(trackName, viewName);
};
