var filterBy = function(tableType) {
$.fn.dataTableExt.afnFiltering.length = 0;
$.fn.dataTable.ext.search.push(
    function( settings, data, dataIndex ) {        
        var type = data[1]; // use data for the age column
 
        if ( type == tableType || tableType=='All' )
        {
            return true;
        }
        return false;
    }
);
}

$(document).ready(function() {
	var activeObject;
    var table = $('#constraint_table').DataTable( {
        lengthChange: false,		
		bSort: true,
		paging: config.pagination,
		pageLength: 50,
		autoWidth: true,
		bDeferRender: true,
		bProcessing: true,
		order: [[ 0, "asc" ]]						
    } );
 
    table.buttons().container()
        .appendTo('#constraint_table_wrapper .col-sm-6:eq(0)' );    	
} );
