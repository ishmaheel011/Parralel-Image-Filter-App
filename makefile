JAVAC=/usr/bin/javac
.SUFFIXES: .java .class
SRCDIR=src
BINDIR=bin

$(BINDIR)/%.class:$(SRCDIR)/%.java
        $(JAVAC) -d $(BINDIR)/ -cp $(BINDIR) $<

CLASSES= Entry.class BinaryTreeNode.class BTQueueNode.class BTQueue.class BinaryTree.class BinarySearchTree.class AVLTre
e.class LSBST.class LSAVL.class LSBSTApp.class LSAVLApp.class
CLASS_FILES=$(CLASSES:%.class=$(BINDIR)/%.class)
default: $(CLASS_FILES)

clean:
        rm $(BINDIR)/*.class