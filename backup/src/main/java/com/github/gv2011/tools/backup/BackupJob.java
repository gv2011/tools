package com.github.gv2011.tools.backup;

import com.github.gv2011.util.icol.IList;

public interface BackupJob {

  IList<FileSystemElement> parts();

}
