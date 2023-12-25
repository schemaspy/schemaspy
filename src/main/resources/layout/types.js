const filterBy = function (functionType) {
    $.fn.dataTableExt.afnFiltering.length = 0;
    $.fn.dataTable.ext.search.push(
        function (settings, data, dataIndex) {
            return data[0].toUpperCase() === functionType || functionType === 'ALL';
        }
    );
};

const getButtons = function () {
    let activeObject;
    let typesOfType = $.unique($.map($('#types_table > tbody > tr'), row => row.cells[0].innerText));
    typesOfType.unshift('All');

    let typeOfTypeButtons = $.map(typesOfType, typeOfType => {
        return {
            text: typeOfType,
            action: function (e, dt, node, config) {
                filterBy(typeOfType.toUpperCase());
                if (activeObject != null) {
                    activeObject.active(false);
                }
                this.active(!this.active());
                activeObject = this;
                dt.draw();
            }
        }
    });
    typeOfTypeButtons.push({
        extend: 'columnsToggle',
        columns: '.toggle'
    })
    return typeOfTypeButtons;
};

$(document).ready(function () {
    let table = $('#types_table').DataTable({
        lengthChange: false,
        ordering: true,
        paging: config.pagination,
        pageLength: 50,
        autoWidth: true,
        processing: true,
        order: [[0, "asc"]],
        buttons: getButtons()
    });

    //schemaSpy.js
    dataTableExportButtons(table);
});