import sys
import json


def print_json(obj, kst, vst, parent_key=''):
    if isinstance(obj, dict):
        for key, value in obj.items():
            new_key = f"{parent_key}.{key}" if parent_key else key
            print_json(value, kst, vst, new_key)
    else:
        kst.add(parent_key)
        vst[parent_key] = obj


def main():
    if not len(sys.argv) == 3:
        return

    template_path = sys.argv[1]
    checking_path = sys.argv[2]

    with open(template_path, encoding="utf-8") as f:
        template = json.loads(f.read())

    with open(checking_path, encoding="utf-8") as f:
        checking = json.loads(f.read())

    template_set = set()
    template_dict = dict()
    print_json(template, template_set, template_dict)

    checking_set = set()
    checking_dict = dict()
    print_json(checking, checking_set, checking_dict)

    diff = template_set.difference(checking_set)

    print("Keys that are missing in " + checking_path + ": ")
    print()

    for d in diff:
        print("Key:", d + ", Value:", template_dict[d])
        print(d, " = ", )


if __name__ == "__main__":
    main()
