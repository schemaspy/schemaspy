var filterBy = function(tableType) {
$.fn.dataTableExt.afnFiltering.length = 0;
$.fn.dataTable.ext.search.push(
    function( settings, data, dataIndex ) {        
        var type = data[1]; // use data for the Type column
 
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
    var table = $('#column_table').DataTable( {
        lengthChange: false,		
		bSort: true,
		paging: config.pagination,
		pageLength: 50,
		autoWidth: true,
		bDeferRender: true,
		bProcessing: true,
		order: [[ 2, "asc" ]],		
		buttons: [ 
					{
						text: 'All',
						action: function ( e, dt, node, config ) {
							filterBy('All');
							if (activeObject != null) {
								activeObject.active(false);
							}
							table.draw();
						}
					},
					{
						text: 'Tables',
						action: function ( e, dt, node, config ) {
							filterBy('Table');
							if (activeObject != null) {
								activeObject.active(false);
							}
							this.active( !this.active() );
							activeObject = this;
							table.draw();
						}
					},
					{
						text: 'Views',
						action: function ( e, dt, node, config ) {
							filterBy('View');
							if (activeObject != null) {
								activeObject.active(false);
							}
							this.active( !this.active() );
							activeObject = this;
							table.draw();
						}
					},
					{
						extend: 'columnsToggle',
						columns: '.toggle'
					}
				]
					
    } );

    //schemaSpy.js
    dataTableExportButtons(table);
} );
