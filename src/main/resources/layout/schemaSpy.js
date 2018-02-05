$(function() {
     var pgurl = window.location.href.substr(window.location.href.lastIndexOf("/")+1);
     $("#navbar-collapse ul li a").each(function(){
          if($(this).attr("href") == pgurl || $(this).attr("href") == '' )
          $(this).parent().addClass("active");
     })
});

function dataTableExportButtons(table) {
   new $.fn.dataTable.Buttons( table, {
       buttons: [
           {
               extend:    'copyHtml5',
               text:      '<i class="fa fa-files-o"></i>',
               titleAttr: 'Copy'
           },
           {
               extend:    'excelHtml5',
               text:      '<i class="fa fa-file-excel-o"></i>',
               titleAttr: 'Excel'
           },
           {
               extend:    'csvHtml5',
               text:      '<i class="fa fa-file-text-o"></i>',
               titleAttr: 'CSV'
           },
           {
               extend:    'pdfHtml5',
               text:      '<i class="fa fa-file-pdf-o"></i>',
               orientation: 'landscape',
               titleAttr: 'PDF'
           }
       ]
   } );

   table.buttons().container()
       .appendTo( '#button_group_one' );

   table.buttons( 1, null ).container()
       .appendTo( '#button_group_two' );
}

 