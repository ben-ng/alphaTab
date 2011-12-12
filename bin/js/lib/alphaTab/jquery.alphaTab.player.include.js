/*
	* This file is part of alphaTab.
	*
	*  alphaTab is free software: you can redistribute it and/or modify
	*  it under the terms of the GNU General Public License as published by
	*  the Free Software Foundation, either version 3 of the License, or
	*  (at your option) any later version.
	*
	*  alphaTab is distributed in the hope that it will be useful,
	*  but WITHOUT ANY WARRANTY; without even the implied warranty of
	*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	*  GNU General Public License for more details.
	*
	*  You should have received a copy of the GNU General Public License
	*  along with alphaTab.  If not, see <http://www.gnu.org/licenses/>.
	*/
/**
	* Required code for the player plugin to work
	*/
	
function onAppletLoaded(noplayer,nomidi) {
	launchAlphaTab(fileSource,noplayer,nomidi,callback)
}

function launchAlphaTab(tabSource,noplayer,nomidi,callback) {
	$("#midiplayer").attr("width",0).attr("height",0);
	var midiPlayer=document.getElementById("midiplayer");
	if(midiPlayer!==undefined && midiPlayer!==null) {
		midiPlayer.setIsMac(BrowserDetect.OS==="Mac");
	}
	
	api = $('div.alphaTab').alphaTab(
		{
			file: tabSource,
			zoom: 1,
			width: $('body').innerWidth() - 30, //Subtract padding (20) then another 10px to avoid horizontal scrollbar forming
			autoSize: false,
			//staves: ["tablature"],
			error: 'Applet loaded, working on the file now...',
            loadCallback: (
            	(noplayer||nomidi) ?
            	(function(song) {
					var playerControls = $('<div class="jqueryui fixedControls player"></div>');
					var tracks = $('<select id="tracks"></select>');
					playerControls.append(tracks);
					
					for (var i = 0; i < song.tracks.length; i++) {
						var trackName=song.tracks[i].name;
						if(trackName.toLowerCase().indexOf("track")==0) {
							trackName=song.tracks[i].channel.getInstrumentName();
						}
						var elm = $('<option value="' + i + '">' + (i+1) + ' - ' + trackName + '</option>');
						if(i == 0) { elm.attr("selected", "selected"); }
						tracks.append(elm);
					}
					$("div.alphaTab").append(playerControls);
					tracks.selectmenu({
				        style:'popup', 
		       			menuWidth:200,
		       			maxHeight:1000,
						positionOptions: {
							my: "left bottom",
							at: "left top",
							offset: "0 0"
						},
						wrapperElement: '<div id="selectWrap" class="jqueryui" />',
						change:function(e) {
							var index = parseInt($('#tracks :selected').val());
							api.tablature.setTrack(api.tablature.track.song.tracks[index]);
						}
					});
					$("#selectWrap").css("margin-left",25);
	            })
	            : undefined
            )
		});
		
	if((noplayer!==undefined && noplayer===true) || (nomidi!==undefined && nomidi===true)) {
		if(callback!==undefined && callback!==null) {
			callback(noplayer,nomidi);
		}
	}
	else {
		//Launch player interface
		launchPlayer();
	}
	return true;
}
function onTickChanged (tickPosition) {
	api.updateCaret(tickPosition);
	return true;
}
function onSequenceStopped(tickPosition) {
	$('#playButton').click();
	api.updateCaret(tickPosition);
	return true;
}
function startJREInstall() {
	deployJava.setInstallerType("online");
	deployJava.installLatestJRE();
}
function launchPlayer() {
	api.player(
	{										// enable the player plugin only after the applet has loaded
		playerTickCallback: "onTickChanged", // !required!
		onAppletLoaded: "onAppletLoaded",     // !required!
		onSequenceStop: 'onSequenceStopped'     // !required!
	});
	return true;
}