$(document).ready(function() {
    $.fn.dataTableExt.afnFiltering.length = 0;
	var options = {
        lengthChange: false,
        ordering: true,
        paging: config.pagination,
        pageLength: 50,
        autoWidth: false,
        processing: true,
        order: [[ 0, "asc" ]]
    }
    $('#fk_table').DataTable(options);
    $('#check_table').DataTable(options);
} );
