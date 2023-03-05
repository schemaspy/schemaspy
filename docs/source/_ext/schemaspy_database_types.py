import os
from os.path import join
from collections import defaultdict

from docutils.frontend import OptionParser
from docutils.utils import new_document
from sphinx.parsers import RSTParser
from sphinx.util.docutils import SphinxDirective


class SchemaSpyDatabaseTypes(SphinxDirective):

    types_path = join('..', 'src', 'main', 'resources', 'org', 'schemaspy', 'types')

    def run(self):
        if not os.getcwd().endswith('docs'):
            self.types_path = join('..', self.types_path)
        db_types_grouped_by_dbms = self.load_database_types()
        table = self.create_table(db_types_grouped_by_dbms)
        return self.parse_rst(table)

    def load_database_types(self):
        db_types_grouped_by_dbms = defaultdict(list)
        with os.scandir(self.types_path) as files:
            for file in files:
                if file.is_file():
                    db_type = self.load_database_type(file)
                    db_types_grouped_by_dbms[db_type.get('dbms')].append(db_type)
        return db_types_grouped_by_dbms

    def load_database_type(self, file):
        with open(file.path) as stream:
            lines_without_comments = [line for line in stream.readlines() if line[0] != "#"]
        merged_lines = self.merge_lines(lines_without_comments)
        db_type = {'cmd_arg': file.name[0:-11]}
        for line in merged_lines:
            key_value = line.split("=", 1)
            if len(key_value) == 2:
                db_type[key_value[0].strip()] = key_value[1].strip()
        return db_type

    @staticmethod
    def merge_lines(lines):
        lines_merged = []
        append = False
        for line in lines:
            if append:
                lines_merged[len(lines_merged) - 1] += line
            else:
                lines_merged.append(line)
            append = line[-1] == "\\"
        return lines_merged

    @staticmethod
    def create_table(db_types_grouped_by_dbms):
        rst_text = ""
        rst_text += ".. flat-table:: \n"
        rst_text += "   :class: clean-table\n"
        rst_text += "   :header-rows: 1\n\n"
        rst_text += "   * - Dbms\n"
        rst_text += "     - Description\n"
        rst_text += "     - Argument `-t`\n"
        for dbms in sorted(db_types_grouped_by_dbms):
            rst_text += "   * - :rspan:`" + str(len(db_types_grouped_by_dbms[dbms]) - 1) + "` " + dbms + "\n"
            for index, db_type in enumerate(db_types_grouped_by_dbms[dbms]):
                if index == 0:
                    rst_text += "     - " + db_type.get('description') + "\n"
                else:
                    rst_text += "   * - " + db_type.get('description') + "\n"
                rst_text += "     - " + db_type.get('cmd_arg') + "\n"
        return rst_text + "\n\n"

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