 $(function () {
            var $chk = $("#grpChkBox input:checkbox"); 
            $chk.prop('checked', true); 
			$chk.click(function () {
                processToggle($(this), $(this).prop('checked'));
            });
        });
		
		function processToggle(onObject, toShow) {
			var $tbl = $("#tab");
            var $tblhead = $("#tab th");
			var colToHide = $tblhead.filter("." + onObject.attr("name"));
			var index = $(colToHide).index();
			if (toShow) $tbl.find('tr :nth-child(' + (index + 1) + ')').show();
			else $tbl.find('tr :nth-child(' + (index + 1) + ')').hide();
		}

		function uncheckAll(divid) {
			var objects = $('#' + divid + ' :checkbox:enabled');
			$.each(objects, function( name, obj ) {
				$(obj).prop('checked', false);	
				processToggle($(obj), false);
			});		
		}

		function checkAll(divid) {
			var objects = $('#' + divid + ' :checkbox:enabled');
			$.each(objects, function( name, obj ) {
				$(obj).prop('checked', true);	
				processToggle($(obj), true);
			});
		}