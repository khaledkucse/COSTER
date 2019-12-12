import scripts.config as config,json, re

config.init()


with open(config.github_projects_builds_path, 'r') as f:
    projects_dict = json.load(f)


jar_count = dict()

for each_project in projects_dict:
    project_detail = projects_dict[each_project]
    if 'depends' in project_detail:
        jar_needed = project_detail['depends']
        for each_jar in jar_needed:
            jar_location = each_jar[4]
            jar_loc_tokens = jar_location.strip().split("/")
            if '.jar' in jar_loc_tokens[len(jar_loc_tokens)-1]:
                jar_name = jar_loc_tokens[len(jar_loc_tokens)-1]
                jar_name_tokens = re.split(r"(-\d|_\d|\d\.\d|-v\d.|_v\d.|v\d.|-build|_build|build|beta|_beta|-beta)(?i)", jar_name)
                jar_token = jar_name_tokens[0].replace('.jar','')

                if jar_token.strip() == '':
                    continue

                if jar_token.strip() in jar_count:
                    cur_jar_info = jar_count[jar_token.strip()]
                    cur_jar_info['Count'] = cur_jar_info['Count']+1
                    if jar_location not in cur_jar_info['Location']:
                        cur_jar_info['Location'].append(jar_location)
                    jar_count[jar_token.strip()] = cur_jar_info
                else:
                    jar_info = dict()
                    jar_info['Count'] = 1
                    locations = [jar_location]
                    jar_info['Location'] = locations
                    jar_count[jar_token.strip()] = jar_info


filtered_jar = dict()
for each_jar in jar_count:
    jar_info = jar_count[each_jar]
    if jar_info['Count'] > 80 and each_jar != 'bin' and each_jar != 'rt' and each_jar != 'msExchange':
        filtered_jar[each_jar] = jar_info


with open(config.jar_files_list_path, 'w') as outfile:
    json.dump(filtered_jar, outfile)
