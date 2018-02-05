function enableAnchors() {
    anchors.options.visible = 'always';
    anchors.add('h3');
}

$(document).ready(function() {
    enableAnchors();

    var table = $('#standard_table').DataTable( {
        lengthChange: false,		
		bSort: false,
		bPaginate: false,
		autoWidth: true,		
		buttons: [ 
					{
						text: 'Related columns',
						action: function ( e, dt, node, config ) {
							$(".relatedKey").toggle();
							this.active( !this.active() );
							table.columns.adjust().draw();
						}
					},
					{
						text: 'Constraint',
						action: function ( e, dt, node, config ) {
							$(".constraint").toggle();
							this.active( !this.active() );
							table.columns.adjust().draw();
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

$(document).ready(function() {
    var indexes = $('#indexes_table').DataTable( {
        lengthChange: false,		
		bPaginate: false,
		bSort: false									
    } );
 
    
} );


 $(function() {
	var $imgs = $('img.diagram');

	$imgs.each(function () {
		eval("$('#"+$(this).attr('id')+"').draggable();")		
	});	 
 });
 
 $.fn.digits = function(){ 
    return this.each(function(){ 
        $(this).text( $(this).text().replace(/(\d)(?=(\d\d\d)+(?!\d))/g, "1 ") ); 
    })
 } 

 $(function() {
	$("#recordNumber").digits();
 });

var codeElement = document.getElementById("sql-script-codemirror");
var editor = null;
if (null != codeElement) {
	editor = CodeMirror.fromTextArea(codeElement, {
		lineNumbers: true,
		mode: 'text/x-sql',
		indentWithTabs: true,
		smartIndent: true,
		lineNumbers: true,
		matchBrackets: true,
		autofocus: true
	});
}