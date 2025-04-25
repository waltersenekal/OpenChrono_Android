import os
import hashlib
import csv
import sys

def calculate_md5(file_path):
    hasher = hashlib.md5()
    with open(file_path, 'rb') as f:
        for chunk in iter(lambda: f.read(4096), b""):
            hasher.update(chunk)
    return hasher.hexdigest()

def calculate_sha1(file_path):
    hasher = hashlib.sha1()
    with open(file_path, 'rb') as f:
        for chunk in iter(lambda: f.read(4096), b""):
            hasher.update(chunk)
    return hasher.hexdigest()

def get_file_size(file_path):
    return os.path.getsize(file_path)

def process_directory(root_dir, base_dir):
    file_info_list = []
    for root, dirs, files in os.walk(root_dir):
        # Ignore specific folders
        dirs[:] = [d for d in dirs if d not in {".idea", ".vscode"}]
        
        for file_name in files:
            # Ignore HTML files
            if file_name.endswith(".html"):
                continue
            
            file_path = os.path.join(root, file_name)
            relative_path = os.path.relpath(file_path, base_dir)  # Make relative to the base directory
            md5 = calculate_md5(file_path)
            sha1 = calculate_sha1(file_path)
            size = get_file_size(file_path)
            file_info_list.append((relative_path.replace("\\", "/"), md5, sha1, size))  # Consistent use of forward slashes
    return file_info_list

def write_to_csv(file_info_list, output_file):
    file_exists = os.path.exists(output_file)
    if file_exists:
        try:
            os.remove(output_file)
        except OSError as e:
            print(f"Error deleting existing file: {e}", file=sys.stderr)
            sys.exit(1)
    try:
        with open(output_file, 'w', newline='') as csvfile:
            writer = csv.writer(csvfile)
            # Write header
            # writer.writerow(["File Path", "MD5", "SHA1", "Size in Bytes"])
            for file_info in file_info_list:
                writer.writerow(file_info)
    except IOError as e:
        print(f"Error writing to CSV file: {e}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    print("Start MD5 calculation, SHA1 calculation, and CSV writing.")
    python_file = sys.argv[0]
    script_directory = os.path.dirname(os.path.abspath(python_file))
    root_directory = os.path.join(script_directory, "html")  # Ensure it's relative to the script's location
    output_csv_file = os.path.join(script_directory, python_file.replace(".py", ".csv"))

    file_info_list = process_directory(root_directory, script_directory)
    write_to_csv(file_info_list, output_csv_file)
    print("MD5 calculation, SHA1 calculation, and CSV writing completed successfully.")