
$(document).ready(function() {
	var activeObject;
    var table = $('#routine_table').DataTable( {
        lengthChange: false,
		bSort: true,
		paging: config.pagination,
		pageLength: 50,
		autoWidth: true,
		bDeferRender: true,
		bProcessing: true,
		order: [[ 0, "asc" ]],
		buttons: [ ]
    } );
 
    table.buttons().container()
        .appendTo('#column_table_wrapper .col-sm-6:eq(0)' );
} );
