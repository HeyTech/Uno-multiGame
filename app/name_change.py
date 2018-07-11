import os
folder_path = os.getcwd()
print(os.getcwd())
for i, filename in enumerate(os.listdir(folder_path)):

    if 'blue' in filename:
        newname = filename.replace('blue','b')
        newname = newname.replace('_' , '')
        #newname = newname[0][0] +'.png'
        print(newname)
        #print(folder_path)
    #new_name = filename.split('_')
    #new_name = new_name[0][0] + new_name[1][0] + '.png'
    #print(filename + ' --> ', new_name)
        os.rename(folder_path +  str('/') + filename, folder_path + str('/') + newname)
    elif 'green' in filename:
         newname = filename.replace('green','g')
         newname = newname.replace('_', '')
         #newname = newname[0][0] + '.png'
         print(newname)
         os.rename(folder_path + str('/') + filename, folder_path + str('/') + newname)

    elif 'red' in filename:
          newname = filename.replace('red','r')
          newname = newname.replace('_', '')
          #newname = newname[0][0] + '.png'
          print(newname)
          os.rename(folder_path + str('/') + filename, folder_path + str('/') + newname)

    elif 'yellow' in filename:
          newname = filename.replace('yellow','y')
          newname = newname.replace('_', '')
          #newname = newname[0][0] + '.png'
          print(newname)
          os.rename(folder_path + str('/') + filename, folder_path + str('/') + newname)

    elif 'gpicker' in filename:
          newname = filename.replace('gpicker', 'gp')
          print(newname)
          os.rename(folder_path + str('/') + filename, folder_path + str('/') + newname)

    elif 'greverse' in filename:
         newname = filename.replace('greverse', 'gr')
         print(newname)
         os.rename(folder_path + str('/') + filename, folder_path + str('/') + newname)

    elif 'gskip' in filename:
         newname = filename.replace('gskip', 'gs')
         print(newname)
         os.rename(folder_path + str('/') + filename, folder_path + str('/') + newname)

    elif 'rpicker' in filename:
        newname = filename.replace('rpicker', 'rp')
        print(newname)
        os.rename(folder_path + str('/') + filename, folder_path + str('/') + newname)

    elif 'rreverse' in filename:
        newname = filename.replace('rreverse', 'rr')
        print(newname)
        os.rename(folder_path + str('/') + filename, folder_path + str('/') + newname)

    elif 'rskip' in filename:
        newname = filename.replace('rskip', 'rs')
        print(newname)
        os.rename(folder_path + str('/') + filename, folder_path + str('/') + newname)

    elif 'ypicker' in filename:
          newname = filename.replace('ypicker', 'yp')
          print(newname)
          os.rename(folder_path + str('/') + filename, folder_path + str('/') + newname)

    elif 'yreverse' in filename:
         newname = filename.replace('yreverse', 'yr')
         print(newname)
         os.rename(folder_path + str('/') + filename, folder_path + str('/') + newname)

    elif 'yskip' in filename:
         newname = filename.replace('yskip', 'ys')
         print(newname)
         os.rename(folder_path + str('/') + filename, folder_path + str('/') + newname)

    elif 'bpicker' in filename:
          newname = filename.replace('bpicker', 'bp')
          print(newname)
          os.rename(folder_path + str('/') + filename, folder_path + str('/') + newname)

    elif 'breverse' in filename:
         newname = filename.replace('breverse', 'br')
         print(newname)
         os.rename(folder_path + str('/') + filename, folder_path + str('/') + newname)

    elif 'bskip' in filename:
         newname = filename.replace('bskip', 'bs')
         print(newname)
         os.rename(folder_path + str('/') + filename, folder_path + str('/') + newname)

    elif 'wild_color_changer' in filename:
          newname = filename.replace('wild_color_changer','wc')
          print(newname)
          os.rename(folder_path + str('/') + filename, folder_path + str('/') + newname)

    elif 'wild_pick_four' in filename:
          newname = filename.replace('wild_pick_four','wp')
          print(newname)
          os.rename(folder_path + str('/') + filename, folder_path + str('/') + newname)

    elif 'card_back' in filename:
          newname = filename.replace('card_back','cb')
          print(newname)
          os.rename(folder_path + str('/') + filename, folder_path + str('/') + newname)









