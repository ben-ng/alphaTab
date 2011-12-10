<?php 
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
$currentDir = dirname(__file__);
require_once($currentDir . '/pathconfig.php');
$title = "Notes";
$description = "This sample shows how to write measures and notes.";
?> 
<!-- Include the Editor Plugin -->
<script language="JavaScript" type="text/javascript" src="<?php echo $alphaTabPath; ?>/jquery.alphaTab.editor.js"></script>
<script language="JavaScript" type="text/javascript">
(function($) {
    $(document).ready(function() {
        $('div.alphaTab').alphaTab().editor();
    });
})(jQuery);
</script>
<h2>Single Notes and Rests</h2>
Notes: <pre>fret.string.duration</pre><br />
Rests: <pre>r.duration</pre>
Duration: <pre>1, 2, 4, 8, 16, 32 or 64</pre>
<div class="alphaTab">
0.6.2 1.5.4 3.4.4 | 5.3.8 5.3.8 5.3.8 5.3.8 r.2
</div>

<h2>Duration Ranges</h2>
<pre>:duration fret.string fret.string ...</pre>
<div class="alphaTab">
:4 2.3 3.3 :8 3.3 4.3 3.3 4.3
</div>

<h2>Chords</h2>
<pre>( fret.string fret.string ...).duration</pre>
<div class="alphaTab">
:4
(0.3{st} 0.4{st}) (3.3{st} 3.4{st}) (5.3 5.4) :8 r (0.3 0.4) |
r (3.3 3.4) r (6.3 6.4) :4 (5.3 5.4){d} r |
(0.3{st} 0.4{st}) (3.3{st} 3.4{st}) (5.3 5.4) :8 r (3.3 3.4) |
r (0.3 0.4) (-.3 -.4).2{d}
</div>

