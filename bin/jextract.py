#!/usr/bin/python3

"""
This script downloads jextract and uses it to generate bindings

Usage:
python3 jextract.py <destination_path> [package]

The main class is called Lib and is located in the package

Requirements:
- Running JDK 21
- linux system
- clang

Why do we need this script?
---------------------------
It downloads jextract automatically and uses it to generate the bindings.

License
-------
MIT
"""

import subprocess
import sys
import time
from pathlib import Path
import urllib.request
import os
import shutil
import tarfile

BASE_FOLDER = Path(__file__).parent.parent
BIN_FOLDER = BASE_FOLDER / "bin"
JEXTRACT_PATH = BIN_FOLDER / "jextract-21"
JEXTRACT_TOOL_PATH = JEXTRACT_PATH / "bin" / "jextract"
JEXTRACT_VERSION = 2


def download_jextract():
    # download jextract
    shutil.rmtree(JEXTRACT_PATH, ignore_errors=True)
    print("Downloading jextract")
    url = (f"https://download.java.net/java/early_access/jextract/"
           f"1/openjdk-21-jextract+1-{JEXTRACT_VERSION}_linux-x64_bin.tar.gz")
    os.makedirs(BIN_FOLDER, exist_ok=True)
    urllib.request.urlretrieve(url, BIN_FOLDER / "jextract.tar.gz")
    # extract jextract
    tar = tarfile.open(BIN_FOLDER / "jextract.tar.gz")
    tar.extractall(BIN_FOLDER)
    tar.close()

    # make jextract executable
    os.chmod(JEXTRACT_PATH, 0o755)
    # remove tar.gz
    (BIN_FOLDER / "jextract.tar.gz").unlink()
    print("Downloaded jextract")
    # set st_mtime to now
    JEXTRACT_PATH.touch()


def ensure_jextract_in_path():
    # download jextract if it doesn't exist or
    # if it is older than 10 days
    if not JEXTRACT_PATH.exists() or (
            JEXTRACT_PATH.stat().st_mtime < (
            time.time() - 10 * 24 * 60 * 60)):
        download_jextract()


ensure_jextract_in_path()

HEADERS = BASE_FOLDER / "misc" / "headers.h"
COMBINED_HEADER = BASE_FOLDER / "misc" / "combined_headers.h"
MODIFIED_HEADER = BASE_FOLDER / "misc" / "modified_headers.h"

def create_combined_header():

    subprocess.check_output(
        f"clang -C -E {HEADERS} -o {COMBINED_HEADER}", shell=True)


def create_modified_header():
    """
    Find lines that match regexp
    "union.* __attribute__\(\(aligned\(8\)\)\);" and
    replace "__attribute__((aligned(8)))" with
    "var{counter} __attribute__((aligned(8)))"
    Store the file in MODIFIED_HEADER
    """
    create_combined_header()
    with open(COMBINED_HEADER) as f:
        lines = f.readlines()
    COMBINED_HEADER.unlink()
    with open(MODIFIED_HEADER, "w") as f:
        counter = 0
        for line in lines:
            if "union" in line and "__attribute__((aligned(8)));" in line:
                line = line.replace("__attribute__((aligned(8)));",
                                    f"var{counter} __attribute__((aligned(8)));\n")
                counter += 1
            f.write(line)


def assert_java21():
    """ assert that we are running JDK 21 by calling java -version """
    try:
        output = subprocess.check_output("java -version", shell=True,
                                         stderr=subprocess.STDOUT).decode()
        assert "version \"21" in output, \
            "Please run this script with JDK 21"
    except FileNotFoundError:
        print("Please install JDK 21 and run this script with JDK 21")
        sys.exit(1)


def run_jextract(dest_path: Path, package: str = "", name: str = "Lib",
                 delete_dest_path: bool = True):
    assert_java21()
    print("Running jextract")
    create_modified_header()
    del_path = dest_path
    if package:
        del_path = dest_path / package.replace(".", "/")
    if delete_dest_path:
        shutil.rmtree(del_path, ignore_errors=True)
    os.makedirs(dest_path, exist_ok=True)
    subprocess.check_call(
        f"{JEXTRACT_TOOL_PATH} {MODIFIED_HEADER} "
        f"--source --output {dest_path} {'-t ' + package if package else ''} "
        f"--header-class-name {name}",
        shell=True)


if __name__ == "__main__":

    if 1 < len(sys.argv) <= 4:
        run_jextract(Path(sys.argv[1]),
                     sys.argv[2] if len(sys.argv) >= 3 else "",
                     sys.argv[3] if len(sys.argv) == 4 else "Lib")
    else:
        print("Usage: jextract.py <destination_path> [package] [name]")
