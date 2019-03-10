	$.tablesorter.addParser({
		id: 'error',
		is: function(s) {
			return true;
		},
	format: function(s) {
		return s.toLowerCase().replace(/-/,99999);
	},
		type: 'numeric'
	});

	$(function() {
	$("#sortable_sym").tablesorter({
		headers: { 
			1:{sorter:'error'}, 2:{sorter:'error'}, 3:{sorter:'error'}, 4:{sorter:'error'}, 5:{sorter:'error'}, 6:{sorter:'error'}, 7:{sorter:'error'}, 8:{sorter:'error'}, 9:{sorter:'error'}, 10:{sorter:'error'}, 
			11:{sorter:'error'}, 12:{sorter:'error'}, 13:{sorter:'error'}, 14:{sorter:'error'}, 15:{sorter:'error'}, 16:{sorter:'error'}, 17:{sorter:'error'}, 18:{sorter:'error'}, 19:{sorter:'error'}, 20:{sorter:'error'}, 
			21:{sorter:'error'}, 22:{sorter:'error'}, 23:{sorter:'error'}, 24:{sorter:'error'}, 25:{sorter:'error'}, 26:{sorter:'error'}, 27:{sorter:'error'}, 28:{sorter:'error'}, 29:{sorter:'error'}, 30:{sorter:'error'} 
			}		});
	});
	$(function() {
	$("#sortable_asym").tablesorter({
		headers: { 
			1:{sorter:'error'}, 2:{sorter:'error'}, 3:{sorter:'error'}, 4:{sorter:'error'}, 5:{sorter:'error'}, 6:{sorter:'error'}, 7:{sorter:'error'}, 8:{sorter:'error'}, 9:{sorter:'error'}, 10:{sorter:'error'}, 
			11:{sorter:'error'}, 12:{sorter:'error'}, 13:{sorter:'error'}, 14:{sorter:'error'}, 15:{sorter:'error'}, 16:{sorter:'error'}, 17:{sorter:'error'}, 18:{sorter:'error'}, 19:{sorter:'error'}, 20:{sorter:'error'}, 
			21:{sorter:'error'}, 22:{sorter:'error'}, 23:{sorter:'error'}, 24:{sorter:'error'}, 25:{sorter:'error'}, 26:{sorter:'error'}, 27:{sorter:'error'}, 28:{sorter:'error'}, 29:{sorter:'error'}, 30:{sorter:'error'} 
			}		});
	});