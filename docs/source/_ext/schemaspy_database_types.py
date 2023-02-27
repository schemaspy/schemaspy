import os
from os.path import join
from collections import defaultdict

from docutils.frontend import OptionParser
from docutils.utils import new_document
from sphinx.parsers import RSTParser
from sphinx.util.docutils import SphinxDirective


class SchemaSpyDatabaseTypes(SphinxDirective):

    def run(self):
        per_dbms = defaultdict(list)
        with os.scandir(join('..', 'src', 'main', 'resources', 'org', 'schemaspy', 'types')) as i:
            for entry in i:
                if entry.is_file():
                    with open(entry.path) as stream:
                        lines_no_comments = [x for x in stream.readlines() if x[0:1] != "#"]
                    lines_merged = []
                    append = False
                    for line in lines_no_comments:
                        if append:
                            lines_merged[len(lines_merged) - 1] += line
                        else:
                            lines_merged.append(line)
                        append = line[-1] == "\\"
                    db_type = {x[0].strip(): x[1].strip() for x in [y.split("=", 1) for y in lines_merged] if len(x) == 2}
                    db_type['arg'] = entry.name[0:-11]
                    per_dbms[db_type.get("dbms")].append(db_type)
        rst_text = ""
        rst_text += ".. flat-table:: \n"
        rst_text += "   :class: clean-table\n"
        rst_text += "   :header-rows: 1\n\n"
        rst_text += "   * - Dbms\n"
        rst_text += "     - Description\n"
        rst_text += "     - Argument `-t`\n"
        for key in sorted(per_dbms):
            rst_text += "   * - :rspan:`" + str(len(per_dbms[key])-1) + "` "+ key +"\n"
            for i, v in enumerate(per_dbms[key]):
                if i == 0:
                    rst_text += "     - " + v.get('description') + "\n"
                else:
                    rst_text += "   * - " + v.get('description') + "\n"
                rst_text += "     - " + v.get('arg') + "\n"
        return self.parse_rst(rst_text + "\n\n")

    def parse_rst(self, text):
        parser = RSTParser()
        parser.set_application(self.env.app)

        settings = OptionParser(
            defaults=self.env.settings,
            components=(RSTParser,),
            read_config_files=True,
        ).get_default_values()
        document = new_document("<rst-doc>", settings=settings)
        parser.parse(text, document)
        return document.children


def setup(app):
    app.add_directive("dbtypes", SchemaSpyDatabaseTypes)
    return {
        'version': '0.1',
        'parallel_read_safe': False,
        'parallel_write_sge': False
    }