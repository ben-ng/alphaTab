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
	* This is a plugin which extends alphaTab with a java midi player.
	*/
(function($, alphaTabWrapper)
{
    alphaTabWrapper.fn.player = function(playerOptions) {
	var playerJar = 'alphaTab.jar';
	var self = this;
    var defaults = {
        player: false,
        playerTickCallback: null,
        createControls: true,
        caret: true,
        measureCaretColor: '#FFF200',
        measureCaretOpacity: 0.25,
        caretOffset: {x: 0, y: 0},
        beatCaretColor: '#4040FF',
        beatCaretOpacity: 0.75,
        autoScroll: true,
        scrollElement: 'html, body',
        scrollAdjustment: 0,
		language:
			{
				play: "Play",
				pause: "Pause",
				stop: "Stop",
				metronome: "Metronome",
				mute: "Mute Track"
			}
    };
	self.lastTickPos = 0;
	var playerOptions = $.extend(defaults, playerOptions);
	if(!navigator.javaEnabled()) {
		alert('Java is not supported by your browser. The player is not available');
		return this;
	}
	//
	// API Functions
	//
	this.selectPreviousMeasure = function() {
		var measure = api.tablature.getCurrentMeasure();
		var prevMeasure=measure.prev();
		if(prevMeasure!=null) {
			self.goToMeasure(prevMeasure);
		}
	}
	this.selectNextMeasure = function() {
		var measure = api.tablature.getCurrentMeasure();
		var nextMeasure=measure.next();
		if(nextMeasure!=null) {
			self.goToMeasure(nextMeasure);
		}
	}
	this.goToMeasure = function(measure) {
		if(measure != null) {
			var wasRunning=self.midiPlayer.isRunning();
			//Pause music if running
			if(wasRunning) { playButton.click(); }
			var tick = 960 + measure.start();
			self.midiPlayer.goTo(tick);
			self.updateCaret(tick,true,false);
			//Resume music
			if(wasRunning) { playButton.click(); }
		}
	}
	this.updatePlayer = function(song) {
		var songData = alphatab.midi.MidiDataProvider.getSongMidiData(song, self.factory, self.tablature.viewLayout._map);
		if(self.midiPlayer.isActive()) {
			self.midiPlayer.updateSongData(songData);
			self.updateCaret(0);
			var tracks = $('#tracks');
			tracks.find('option').remove();
			for (var i = 0; i < song.tracks.length; i++) {
				var trackName=song.tracks[i].name;
				if(trackName.toLowerCase().indexOf("track")==0) {
					trackName=song.tracks[i].channel.getInstrumentName();
				}
				var elm = $('<option value="' + i + '">' + (i+1) + ' - ' + trackName + '</option>');
				if(i == 0) { elm.attr("selected", "selected"); }
				tracks.append(elm);
			}
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
					if(self.midiPlayer.isActive()) {
						var wasRunning=self.midiPlayer.isRunning();
						//Pause music if its running
						if(wasRunning) { $('#playButton').click(); }
						var index = parseInt($('#tracks :selected').val());
						api.tablature.setTrack(api.tablature.track.song.tracks[index]);
						$("#muteCheck").attr("checked",self.midiPlayer.getTrackMute(index+1)).button("refresh");
						self.updateCaret(self.lastTickPos, true);
						//Restart music if its running
						if(wasRunning) { $('#playButton').click(); }
					}
					else { alert("The player has not loaded yet."); }
				}
			});
		}
		else {
		// TODO: repeat loading only 3 times and then show a loading error.
		// Note: probably not needed anymore since the applet must have loaded to reach this point..
		setTimeout(function() { self.updatePlayer(song); }, 1000); }
	}
	this.loadCallbacks.push(this.updatePlayer);
	this.updateCaret = function(tickPos, forced, scroll) {
		self.lastTickPos = tickPos;
		forced = forced === true;
		scroll = !(scroll === false);
		setTimeout(function() { self.tablature.notifyTickPosition(tickPos, forced, scroll); }, 1);
	}
	//
	// Create UI
	//
	var playerControls = $('<div class="jqueryui fixedControls player"></div>');
	var applet = document.getElementById("midiplayer");
	self.playerControls = playerControls[0];
	self.midiPlayer = applet;
	self.el.append(playerControls);
	// create controls
	if(playerOptions.createControls) {
		var playButton = $('<input type="button" id="playButton" class="playerLeftFloat play" value="' + playerOptions.language.play + '" />');
		var metronomeCheck = $('<input id="metronomeCheck" type="checkbox" class="playerRightFloat metronome" />');
		var muteCheck = $('<input id="muteCheck" type="checkbox" class="playerLeftFloat mute" />');
		var trackSelect = $('<select id="tracks"><option value="">Tab is loading...</option></select>');
		var tempoSlider = $('<div id="slider-range-min"></div>');
		var tempoWrapper = $('<div id="sliderWrap"></div>');
		var sliderControl = $('<div id="sliderControl" class="playerLeftFloat"></div>');
		playerControls.append(playButton);
		playerControls.append(trackSelect);
		trackSelect.hide();
		playerControls.append(muteCheck);
		playerControls.append('<label class="playerLeftFloat" for="muteCheck">' + playerOptions.language.mute + '</label>');
		tempoWrapper.append(tempoSlider);
		sliderControl.append('<span id="tempoView">1X</span>');
		sliderControl.append(tempoWrapper);
		playerControls.append(sliderControl);
		playerControls.append(tempoWrapper);
		playerControls.append(metronomeCheck);
		playerControls.append('<label class="playerRightFloat" for="metronomeCheck">' + playerOptions.language.metronome + '</label>');
		// hook up events
		var togglePlay = function(e) {
			if(self.midiPlayer.isActive()) {
				metronomeCheck.change();
				if(playButton.attr('value') === playerOptions.language.play) {
					self.midiPlayer.play();
				}
				else {
					self.midiPlayer.pause();
				}
			}
			else alert("The player has not loaded yet.");
			if(e !== undefined && e !== null && e.stopPropagation) {
				e.stopPropagation();
				e.preventDefault();
				return false;
			}
		};
		self.togglePlay = togglePlay;
		//jQuery UI
		playButton.button(
			{
				text: false,
				icons:
					{
						primary: "ui-icon-play"
					}
			}).click(function() {
			togglePlay();
			var options;
			if(self.midiPlayer.isRunning()) { options =
				{
					label: playerOptions.language.pause,
					icons:
						{
							primary: "ui-icon-pause"
						}
				}; }
			else { options =
				{
					label: playerOptions.language.play,
					icons:
						{
							primary: "ui-icon-play"
						}
				}; }
			$(this).button("option", options);
		});
		$( "#slider-range-min" ).slider({
			range: "min",
			value: 100,
			min: 20,
			max: 200,
			step: 10,
			slide: function( event, ui ) {
				var displayText = Math.round(ui.value/10)/10;
				if(displayText===1) {
					displayText="1.0";
				}
				if(displayText===2) {
					displayText="2.0";
				}
				$( "#tempoView" ).text(displayText + "X");
				self.midiPlayer.setTempoFactor(ui.value/100);
			}
		});
		$( "#tempoView" ).text("1.0X");
		//Let the spacebar toggle playback of the song
		$(document).bind('keyup', 'p', function(){playButton.click();});
		$(document).bind('keyup', 'right', function(){self.selectNextMeasure();});
		$(document).bind('keyup', 'left', function(){self.selectPreviousMeasure();});
		trackSelect.click(function(e) {
			if(self.midiPlayer.isActive() && self.midiPlayer.isRunning()) { togglePlay(e); }
		})
		metronomeCheck.button().change(function() {
			if(self.midiPlayer.isActive()) {
				var enabled = metronomeCheck.attr('checked') ? true : false;
				self.midiPlayer.setMetronomeEnabled(enabled);
			}
			else { alert("The player has not loaded yet."); }
		});
		;
		muteCheck.button().change(function() {
			if(self.midiPlayer.isActive()) {
				var enabled = muteCheck.attr('checked') ? true : false;
				self.midiPlayer.setTrackMute(self.tablature.track.number,enabled);
				console.log("mute "+(self.tablature.track.number)+(enabled?"true":"false"));
			}
			else { alert("The player has not loaded yet."); }
		});
		;
		// Sets the player to the start of the measure clicked
		$(this.canvas).click(function(e) {
			self.el.focus();
			var offsets = $(this).offset();
			var x = e.pageX - offsets.left;
			var y = e.pageY - offsets.top;
			var measure = self.tablature.viewLayout.getMeasureAt(x, y);
			if(measure != null) {
				self.goToMeasure(measure);
			}
		});
	}
	// create carets
	if(playerOptions.caret) {
		var measureCaret = $('<div class="measureCaret"></div>');
		var beatCaret = $('<div class="beatCaret"></div>');
		// set styles
		measureCaret.css(
			{
				'opacity': playerOptions.measureCaretOpacity,
				'position': 'absolute',
				background: playerOptions.measureCaretColor
			});
		beatCaret.css(
			{
				'opacity': playerOptions.beatCaretOpacity,
				'position': 'absolute',
				background: playerOptions.beatCaretColor
			});
		measureCaret.width(0);
		beatCaret.width(0);
		measureCaret.height(0);
		beatCaret.height(0);
		this.el.append(measureCaret);
		this.el.append(beatCaret);
	}
	this.tablature.onCaretChanged = function(beat, forced, scroll) {
		var x = $(self.canvas).offset().left + parseInt($(self.canvas).css("borderLeftWidth"), 10);
		var y = $(self.canvas).offset().top;
		y += beat.measure.staveLine.y;
		var measureX = x + beat.measure.staveLine.x + beat.measure.x;
		measureCaret.offset(
			{
				top: y,
				left: measureX
			});
		measureCaret.width(beat.measure.width + beat.measure.spacing);
		measureCaret.height(beat.measure.staveLine.getHeight());
		var noteSize = alphatab.tablature.drawing.DrawingResources.getScoreNoteSize(self.tablature.viewLayout, false);
		var beatX = x + beat.fullX() + noteSize.x / 2;
		beatCaret.offset(
			{
				top: y,
				left: beatX
			});
		beatCaret.width(3);
		beatCaret.height(measureCaret.height());
		if(forced===true || (scroll && beat.isFirstOfLine() && playerOptions.autoScroll)) {
			var scrollPos = y - ($(window).height() - beatCaret.height()) / 2;
			if(scrollPos < 0) { scrollPos = 0; }
			$(playerOptions.scrollElement).stop(true, false).animate(
				{
					scrollTop: scrollPos
				}, 300);
		}
	}
	// load current song
	if(this.tablature.track != null) { this.updatePlayer(this.tablature.track.song); }
	return this;
} })(jQuery,alphaTabWrapper);