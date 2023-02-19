import os
from os.path import join

from docutils.frontend import OptionParser
from docutils.utils import new_document
from sphinx.parsers import RSTParser
from sphinx.util.docutils import SphinxDirective


class SchemaSpyDatabaseTypes(SphinxDirective):

    def run(self):
        db_types = dict()
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
                    db_types[entry.name[0:-11]] = {x[0]: x[1] for x in [y.split("=", 1) for y in lines_merged] if len(x) == 2}
        rst_text = ""
        for (k,v) in db_types.items():
            rst_text += "| **" + v.get('description', 'missing').strip() + "**\n"
            rst_text += "| \t ``-t " + k + "``\n\n"
        return self.parse_rst(rst_text)

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