#!/usr/bin/env python3
import xml.etree.ElementTree as ET

tree = ET.parse('build/reports/jacoco/merged/jacocoMergedReport.xml')
root = tree.getroot()

print("=== TOP 40 CLASSES BY UNCOVERED LINES ===")
classes = []
for pkg in root.findall('.//package'):
    pkg_name = pkg.get('name').replace('es/pedrazamiguez/splittrip/', '')
    for cls in pkg.findall('class'):
        cls_name = cls.get('name').split('/')[-1]
        for counter in cls.findall('counter'):
            if counter.get('type') == 'LINE':
                missed = int(counter.get('missed'))
                covered = int(counter.get('covered'))
                total = missed + covered
                pct = (covered / total * 100) if total > 0 else 0
                if missed > 0:
                    classes.append((pkg_name, cls_name, missed, covered, total, pct))

classes.sort(key=lambda x: -x[2])
for pkg, cls, missed, covered, total, pct in classes[:40]:
    print("  %4d / %4d (%5.1f%%) %s/%s" % (missed, total, pct, pkg, cls))

print()
for counter in root.findall('counter'):
    ctype = counter.get('type')
    missed = int(counter.get('missed'))
    covered = int(counter.get('covered'))
    total = missed + covered
    if ctype == 'LINE':
        need = max(0, int(0.80 * total) - covered)
        print("LINE: %d/%d = %.1f%%. Need %d more lines for 80%%" % (covered, total, covered/total*100, need))
    if ctype == 'BRANCH':
        need = max(0, int(0.70 * total) - covered)
        print("BRANCH: %d/%d = %.1f%%. Need %d more branches for 70%%" % (covered, total, covered/total*100, need))

