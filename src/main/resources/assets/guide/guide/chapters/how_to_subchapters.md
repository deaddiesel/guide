§6Chapter 3: Creating Subchapters

§7Subchapters create a collapsible drop-down list in the sidebar.

§7Step 1: The @submenu Tag
§7In the main chapter file, add: §6@submenu: file1, file2, file3

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§7EXAMPLE:
§7In file §6how_to_subchapters.md§7: §6@submenu: sub_demo1, sub_demo2, sub_demo3

§l§6━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

§7Step 2: Create the Files
§7In the §6chapters/§7 folder, create:

§6• sub_demo1.md
§6• sub_demo2.md
§6• sub_demo3.md

§7Important Note:

§7• Names after §6@submenu: §7must §6match §7the actual file names
§7• You §6do not§7 need to list these subchapters in §6index.md§7
§7• Subchapters will appear indented and expand on click

@submenu: sub_demo1, sub_demo2, sub_demo3