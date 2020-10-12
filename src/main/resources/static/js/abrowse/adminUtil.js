function changeTrackGroup() {
    var genome = $("#genome").val();
    $.ajax({
        type: "POST",
        url: "/admin/getCurrentConfigGenome",
        data: {
            genome: genome
        },
        async: false,
        success: function (res) {
            console.log(res);
            var trackGroupMap = res.currenConfgGenome.trackGroupMap
            $("#trackGroupName").html("");
            for (var key in trackGroupMap) {
                var trackGroup = trackGroupMap[key];
                var opt = "<option  value='" + trackGroup.name + "'>" + trackGroup.displayName + "</option>";
                $("#trackGroupName").append(opt);
            }
            $("#trackGroupName").selectpicker('refresh');
        },
        dataType: "json",
        context: this
    });
}